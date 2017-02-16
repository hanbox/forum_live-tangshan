# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models
import datetime
from django.utils.timezone import utc


class Migration(migrations.Migration):

    dependencies = [
        ('live', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='user_session',
            name='cover',
            field=models.CharField(default=datetime.datetime(2016, 10, 20, 4, 51, 42, 497000, tzinfo=utc), max_length=255),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='user_session',
            name='session_pull',
            field=models.CharField(default='', max_length=100),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='user_session',
            name='session_push',
            field=models.CharField(default='', max_length=100),
            preserve_default=False,
        ),
    ]
