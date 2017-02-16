# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('mine', '0001_initial'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='user_mine',
            name='userid',
        ),
        migrations.AddField(
            model_name='user_mine',
            name='phone',
            field=models.CharField(default=0, max_length=11),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='user_mine',
            name='username',
            field=models.CharField(default=0, max_length=10),
            preserve_default=False,
        ),
    ]
