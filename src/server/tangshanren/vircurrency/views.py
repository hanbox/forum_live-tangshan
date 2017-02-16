# -*- coding: utf-8 -*-

from __future__ import unicode_literals
from django.http import HttpResponse
from django.shortcuts import render

from django.contrib.auth import get_user_model
from vircurrency.models import VirAccount

# Create your views here.
def index(request):
    return render(request, 'h5_mobile_mymain.html', {})

def sendGold(request):
    if request.method == "GET":
    	userid_id = request.GET.get('user_id')
    	info = VirAccount.objects.get(userid=userid_id)
    	count_pay = request.GET.get('count_pay')
        if info.balance < float(count_pay):
            return HttpResponse('out')
    	info.balance -= float(count_pay)
        info.save();

    	auth_id = request.GET.get('auth_id')
    	toInfo = VirAccount.objects.get(userid=auth_id)
    	toInfo.balance += float(count_pay)
        toInfo.save();

    return HttpResponse('ok')

def recharge(request):
    if request.method == "GET":
        userid_id = request.GET.get('user_id')
        info = VirAccount.objects.get(userid=userid_id)
        count_pay = request.GET.get('count_pay')
        new_count_pay = float(count_pay) * 100
        # print "userid:" + info.balance;
        # print "count:" + new_count_pay;
        info.balance += float(new_count_pay)
        info.save();
        
    return HttpResponse('ok')