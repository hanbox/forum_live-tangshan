# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('mine', '0004_user_mine_headimgurl'),
    ]

    operations = [
        migrations.AddField(
            model_name='user_mine',
            name='login_src',
            field=models.CharField(default=0, max_length=10),
            preserve_default=False,
        ),
    ]
