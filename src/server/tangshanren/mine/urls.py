# -*- coding: utf-8 -*-

from __future__ import unicode_literals

from django.conf.urls import include, url

from . import views

urlpatterns = [
    url(r'^$', views.index, name='index'),
    url(r'^refound/', views.refound, name='refound'),
    url(r'^chklogin/', views.chack_login, name='chack_login'),
    url(r'^getuserinfo/', views.getUserInfo, name='get_userinfo'),
]
