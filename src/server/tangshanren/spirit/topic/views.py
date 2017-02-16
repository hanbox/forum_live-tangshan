# -*- coding: utf-8 -*-

from __future__ import unicode_literals

from django.contrib.auth.decorators import login_required
from django.shortcuts import render, redirect, get_object_or_404
from django.http import HttpResponsePermanentRedirect
from django.core.urlresolvers import reverse
from djconfig import config

from ..core.utils.paginator import paginate, yt_paginate
from ..core.utils.ratelimit.decorators import ratelimit
from ..category.models import Category
from ..comment.models import MOVED
from ..comment.forms import CommentForm
from ..comment.utils import comment_posted
from ..comment.models import Comment
from .models import Topic
from .forms import TopicForm
from . import utils

from adconfig.models import Ad
from mine.models import user_mine

@login_required
@ratelimit(rate='1/10s')
def publish(request, category_id=None):
    if category_id:
        get_object_or_404(
            Category.objects.visible(),
            pk=category_id)

    user = request.user

    if request.method == 'POST':
        form = TopicForm(user=user, data=request.POST)
        cform = CommentForm(user=user, data=request.POST)

        if (all([form.is_valid(), cform.is_valid()]) and
                not request.is_limited()):
            if not user.st.update_post_hash(form.get_topic_hash()):
                return redirect(
                    request.POST.get('next', None) or
                    form.get_category().get_absolute_url())

            # wrap in transaction.atomic?
            topic = form.save()
            cform.topic = topic
            comment = cform.save()
            comment_posted(comment=comment, mentions=cform.mentions)
            return redirect(topic.get_absolute_url())
    else:
        form = TopicForm(user=user, initial={'category': category_id})
        cform = CommentForm()

    context = {
        'form': form,
        'cform': cform}

    return render(request, 'spirit/topic/publish_mobile.html', context)


@login_required
def update(request, pk):
    topic = Topic.objects.for_update_or_404(pk, request.user)

    if request.method == 'POST':
        form = TopicForm(user=request.user, data=request.POST, instance=topic)
        category_id = topic.category_id

        if form.is_valid():
            topic = form.save()

            if topic.category_id != category_id:
                Comment.create_moderation_action(user=request.user, topic=topic, action=MOVED)

            return redirect(request.POST.get('next', topic.get_absolute_url()))
    else:
        form = TopicForm(user=request.user, instance=topic)

    context = {'form': form, }

    return render(request, 'spirit/topic/update_mobile.html', context)


def detail(request, pk, slug):
    topic = Topic.objects.get_public_or_404(pk, request.user)

    if topic.slug != slug:
        return HttpResponsePermanentRedirect(topic.get_absolute_url())

    utils.topic_viewed(request=request, topic=topic)

    comments = Comment.objects\
        .for_topic(topic=topic)\
        .with_likes(user=request.user)\
        .with_polls(user=request.user)\
        .order_by('date')

    comments = paginate(
        comments,
        per_page=config.comments_per_page,
        page_number=request.GET.get('page', 1)
    )

    context = {
        'topic': topic,
        'comments': comments
    }

    return render(request, 'spirit/topic/detail.html', context)

def detail_mobile(request, pk, slug):
    topic = Topic.objects.get_public_or_404(pk, request.user)

    if topic.slug != slug:
        return HttpResponsePermanentRedirect(topic.get_absolute_url())

    utils.topic_viewed(request=request, topic=topic)

    comments = Comment.objects\
        .for_topic(topic=topic)\
        .with_likes(user=request.user)\
        .with_polls(user=request.user)\
        .order_by('date')

    comments = paginate(
        comments,
        per_page=config.comments_per_page,
        page_number=request.GET.get('page', 1)
    )

    curuser = request.user
    info = user_mine.objects.filter(username=curuser.username)

    state = 0
    if len(comments[0].user.username) > 10:
        state=1

    if len(info)>0:
        context = {
            'topic': topic,
            'comments': comments,
            'info':info[0],
            'state':state,
        }

        return render(request, 'spirit/topic/h5_mobile_communitydetail.html', context)
    else:
        context = {
            'topic': topic,
            'comments': comments
        }

        return render(request, 'spirit/topic/h5_mobile_communitydetail.html', context)


def index_active(request):
    # user = request.user
    # if not request.user.is_authenticated():
    #     return redirect("/user/login/?next=/", reverse('spirit:user:auth:login'))

    categories = Category.objects\
        .visible()\
        .parents()

    topics = Topic.objects\
        .visible()\
        .global_()\
        .with_bookmarks(user=request.user)\
        .order_by('-is_globally_pinned', '-last_active')\
        .select_related('category')

    topics = yt_paginate(
        topics,
        per_page=config.topics_per_page,
        page_number=request.GET.get('page', 1)
    )

    context = {
        'categories': categories,
        'topics': topics
    }

    return render(request, 'spirit/topic/active.html', context)

def index_active_mobile(request):
    # user = request.user
    # if not request.user.is_authenticated():
    #     return redirect("/user/login/?next=/", reverse('spirit:user:auth:login'))

    categories = Category.objects\
        .visible()\
        .parents()

    topics = Topic.objects\
        .visible()\
        .global_()\
        .with_bookmarks(user=request.user)\
        .order_by('-is_globally_pinned', '-last_active')\
        .select_related('category')

    topics = yt_paginate(
        topics,
        per_page=config.topics_per_page,
        page_number=request.GET.get('page', 1)
    )

    ad = Ad.objects.filter(itype=0, istate=0)

    curuser = request.user
    info = user_mine.objects.filter(username=curuser.username)
    choices = {'4': '唐山市区', '5': '乐亭', '6': '滦南','7': '迁安','8': '丰南','9': '唐海','10': '遵化','11': '迁西','13': '丰润','15': '滦县','16': '玉田','17': '开平'}
    local = "未登录"
    state = 0
    if len(curuser.username) > 10:
        state=1
    if len(info)>0:
        local = str(choices[str(info[0].locol_id)]).decode("UTF-8")
        context = {
            'categories': categories,
            'topics': topics,
            'ad': ad,
            'local': local,
            'info':info[0],
            'state':state,
        }
        return render(request, 'spirit/topic/h5_mobile_communitymain.html', context)
    else:
        context = {
            'categories': categories,
            'topics': topics,
            'ad': ad,
            'local': local,
            'state':state,
        }
        return render(request, 'spirit/topic/h5_mobile_communitymain.html', context)

    # return render(request, 'spirit/topic/active.html', context)
    

def add_friends(request, friendsid):
    categories = Category.objects\
        .visible()\
        .parents()

    topics = Topic.objects\
        .visible()\
        .global_()\
        .with_bookmarks(user=request.user)\
        .order_by('-is_globally_pinned', '-last_active')\
        .select_related('category')

    topics = yt_paginate(
        topics,
        per_page=config.topics_per_page,
        page_number=request.GET.get('page', 1)
    )

    context = {
        'categories': categories,
        'topics': topics
    }

    return render(request, 'spirit/topic/active.html', context)
