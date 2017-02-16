# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import migrations, models
import django.utils.timezone


class Migration(migrations.Migration):

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Ad',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('zone_id', models.IntegerField()),
                ('title', models.CharField(max_length=100)),
                ('url', models.CharField(max_length=100)),
                ('istate', models.IntegerField()),
                ('itype', models.IntegerField()),
                ('index', models.IntegerField()),
                ('imgp', models.ImageField(upload_to=b'imgpath', blank=True)),
                ('last_active', models.DateTimeField(default=django.utils.timezone.now, verbose_name=b'last_active')),
            ],
        ),
    ]
