# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('live', '0002_auto_20161020_1252'),
    ]

    operations = [
        migrations.AddField(
            model_name='user_session',
            name='chat_id',
            field=models.CharField(default=0, max_length=100),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='user_session',
            name='room_id',
            field=models.CharField(default=0, max_length=100),
            preserve_default=False,
        ),
    ]
