# -*- coding: utf-8 -*-
from .models import user_session
from django.http import HttpResponse
import socket
from live_support.models import  Chat
from django.shortcuts import render
from adconfig.models import Ad
from mine.models import user_mine
from django.shortcuts import render, redirect
from django.core.urlresolvers import reverse

from django.contrib.auth import get_user_model
from vircurrency.models import VirAccount

User = get_user_model()

import sys
reload(sys)
sys.setdefaultencoding( "utf-8" )
# Create your views here.
def index(request):
    ad = Ad.objects.filter(itype=1)
    live_session = user_session.objects.filter(istate=0)

    curuser = request.user
    info = user_mine.objects.filter(username=curuser.username)
    choices = {'4': '唐山市区', '5': '乐亭', '6': '滦南','7': '迁安','8': '丰南','9': '唐海','10': '遵化','11': '迁西','13': '丰润','15': '滦县','16': '玉田','17': '开平'}
    local = "未登录"
    if info:
        local = str(choices[str(info[0].locol_id)]).decode("UTF-8")

    return render(request, 'h5_mobile_live_main.html', {'live_session':live_session, 'ad':ad, 'local':local,})
    
def iwantlive(request):

    return render(request, 'h5_mobile_live_wantlive.html', {})

def createroom(request):
    if request.method == "POST":
        room = user_session()
        roomname = request.POST['roomname']
        # cover = request.FILES['image']
        room.title =  roomname.decode('utf-8')
        room.playCount = 998
        room.auth_id = request.user.id
        # room.cover = cover
        
        s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        s.connect(('127.0.0.1',8001))
        frame = 'createroom'
        s.send(frame + '/' + roomname + '/' + 'live.rtmp_forward_only' + '/' + '-' + '/' + '-' + '/' + '-' + '/')

        while True:    
            data = s.recv(512)  
            if len(data)>0:    
                recvdata =  data.split('/*****/')
                room.session_push = recvdata[0]
                room.session_pull = recvdata[1]
                room.session_id = recvdata[2]
                break
            
        s.close()

        user = request.user
        chat = Chat()
        chat.name = "live" + str(request.user.id)
        chat.details = "live" + str(request.user.id)
        chat.save()

        room.room_id = chat.hash_key
        room.chat_id = chat.id
        room.istate = 0
        room.save()

        roomid = chat.hash_key
        chatid = chat.id

        return render(request, 'h5_mobile_live_wantlive.html',{'status':"true", "session_push":room.session_push, "roomid": roomid, "chatid": chatid})
    else:
        return render(request, 'h5_mobile_live_wantlive.html',{'status':"false"})

def createroom_ucloud(request):
    if request.method == "POST":
        room = user_session()
        roomname = request.POST['roomname']
        # cover = request.FILES['image']
        room.title =  roomname.decode('utf-8')
        room.playCount = 998
        room.auth_id = request.user.id
        # room.cover = cover
        
        # s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        # s.connect(('127.0.0.1',8001))
        # frame = 'createroom'
        # s.send(frame + '/' + roomname + '/' + 'live.rtmp_forward_only' + '/' + '-' + '/' + '-' + '/' + '-' + '/')

        # while True:    
        #     data = s.recv(512)  
        #     if len(data)>0:    
        #         recvdata =  data.split('/*****/')
        #         room.session_push = recvdata[0]
        #         room.session_pull = recvdata[1]
        #         room.session_id = recvdata[2]
        #         break
            
        # s.close()
        # random_id = random.randint(0, 10000)
        room.session_push = room.auth_id
        room.session_pull = room.auth_id
        room.session_id = room.auth_id

        user = request.user
        chat = Chat()
        chat.name = "live" + str(request.user.id)
        chat.details = "live" + str(request.user.id)
        chat.save()

        room.room_id = chat.hash_key
        room.chat_id = chat.id
        room.istate = 0

        info = user_mine.objects.filter(username=user.username)
        room.local = info[0].locol_id;
        room.save()

        roomid = chat.hash_key
        chatid = chat.id

        return render(request, 'h5_mobile_live_wantlive.html',{'status':"true", "session_push":room.session_push, "roomid": roomid, "chatid": chatid})
    else:
        return render(request, 'h5_mobile_live_wantlive.html',{'status':"false"})

def delroom(request):
    if request.method == "GET":
        session_push = request.GET.get('session_push')
        session = user_session.objects.get(session_push=session_push)
        session.istate = 3     # del
        session.save()

        #del session from baidu 
        s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
        s.connect(('127.0.0.1',8001))
        frame = 'delroom'
        s.send(frame + '/' + session.session_id + '/' + "tmptmptmp")

        while True:    
            data = s.recv(512)  
            if len(data)>0:    
                break
            
        s.close()

        

    return HttpResponse('ok')

def delroom_ucloud(request):
    if request.method == "GET":
        room_id = request.GET.get('room_id')
        print "1111111111111" + room_id
        session = user_session.objects.get(room_id=room_id)
        session.istate = 3     # del
        session.save()
        

    return HttpResponse('ok')
