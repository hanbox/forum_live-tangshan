# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('mine', '0003_user_mine_nickname'),
    ]

    operations = [
        migrations.AddField(
            model_name='user_mine',
            name='headimgurl',
            field=models.CharField(default=0, max_length=100),
            preserve_default=False,
        ),
    ]
