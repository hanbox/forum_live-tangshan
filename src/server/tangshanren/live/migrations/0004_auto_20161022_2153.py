# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('live', '0003_auto_20161021_1711'),
    ]

    operations = [
        migrations.AddField(
            model_name='user_session',
            name='auth_id',
            field=models.IntegerField(default=0),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='user_session',
            name='istate',
            field=models.IntegerField(default=0),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='user_session',
            name='session_id',
            field=models.CharField(default=0, max_length=100),
            preserve_default=False,
        ),
    ]
