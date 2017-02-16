# -*- coding: utf-8 -*-

from __future__ import unicode_literals
from django.shortcuts import render
from django.contrib import auth
from django.contrib.auth import get_user_model,login
from vircurrency.models import VirAccount
from mine.models import user_mine
from django.http import HttpResponse

User = get_user_model()

from spirit.user.auth.forms import RegistrationForm

# Create your views here.
def index(request):
    curuser = request.user
    acount = VirAccount.objects.filter(username=curuser.username)
    info = user_mine.objects.filter(username=curuser.username)
    choices = {'4': '唐山市区', '5': '乐亭', '6': '滦南','7': '迁安','8': '丰南','9': '唐海','10': '遵化','11': '迁西','13': '丰润','15': '滦县','16': '玉田','17': '开平'}
    local = "未登录"
    state = 0

    if acount != None and len(acount) > 0 and len(info)>0:
        changeloc = str(request.GET.get('changeloc'))
        if not changeloc == "None":
            info[0].locol_id = int(changeloc)
            info[0].save()

        acount[0].userid = curuser.id
        acount[0].save()
        local = str(choices[str(info[0].locol_id)]).decode("UTF-8")
        info[0].nickname = info[0].nickname.decode("UTF-8");
        if len(curuser.username) > 10:
            state = 1;
        return render(request, 'h5_mobile_mymain.html', {'acount':acount[0], 'local':local, 'user_info':info[0], 'state':state, 'nackname':info[0].nickname})
        # return render(request, 'h5_mobile_mymain.html', {'acount':acount[0]})
    else:
        return render(request, 'h5_mobile_mymain.html', {'local':local})

def refound(request):
    curuser = request.user
    acount = VirAccount.objects.filter(username=curuser.username)
    info = user_mine.objects.filter(username=curuser.username)

    if acount != None and len(acount) > 0:
        acount[0].userid = curuser.id
        acount[0].save()
        info[0].nickname = info[0].nickname.decode("UTF-8");
        if len(curuser.username) > 10:
            state = 1;
        return render(request, 'h5_mobile_refound.html', {'acount':acount[0], 'user_info':info[0], 'nackname':info[0].nickname})
        # return render(request, 'h5_mobile_mymain.html', {'acount':acount[0]})
    else:
        return render(request, 'h5_mobile_mymain.html', {'local':local})

def chack_login(request):
    userid_id = request.GET.get('user_id')
    nickname = request.GET.get('nickname')
    headimgurl = request.GET.get('headimgurl')
    login_src = request.GET.get('login_src')
    user = User.objects.filter(username=userid_id)

    if len(user)>0:
        curuser = auth.authenticate(username=userid_id, password='123456')
        if curuser:
            auth.login(request, curuser)
            session_key = request.session.session_key
            return HttpResponse(session_key)
          
    else:
        mine = user_mine(username=userid_id,locol_id=4,phone="123456",nickname=nickname,headimgurl=headimgurl,login_src=login_src)
        mine.save()

        viruser = VirAccount(username=userid_id)
        viruser.save()

        newuser = User.objects.create_user(username=userid_id,password='123456',email='123@test.com')
        newuser.save

    return HttpResponse('ok')

def getUserInfo(request):
    userid_id = request.GET.get('user_id')
    user = User.objects.filter(id=userid_id)
    
    info = user_mine.objects.filter(username=user[0].username)
    if len(info) >0:
        nickname = info[0].nickname
        headimgurl = info[0].headimgurl

    return HttpResponse(nickname + ',' + headimgurl)


