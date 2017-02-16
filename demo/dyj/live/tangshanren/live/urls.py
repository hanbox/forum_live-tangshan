# -*- coding: utf-8 -*-

from __future__ import unicode_literals

from django.conf.urls import include, url

from . import views

urlpatterns = [
    url(r'^$', views.index, name='index'),
    url(r'^wantlive/$', views.iwantlive, name='iwantlive'),
    url(r'^wantlive/register/', views.register, name='register'),
]
