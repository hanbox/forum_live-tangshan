# -*- coding: utf-8 -*-

from __future__ import unicode_literals

from django.conf.urls import include, url

from . import views

urlpatterns = [
    url(r'^$', views.index, name='index'),
    url(r'^pay/', views.sendGold, name='pay'),
    url(r'^recharge/', views.recharge, name='recharge'),
]
