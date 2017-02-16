# -*- coding: utf-8 -*-

from __future__ import unicode_literals

from django.conf.urls import include, url

from . import views

urlpatterns = [
    url(r'^$', views.check_android, name='android'),
    url(r'^check/android/', views.check_android, name='android'),
    url(r'^check/ios/', views.check_ios, name='ios'),
]
