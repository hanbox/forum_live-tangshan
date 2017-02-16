from django.shortcuts import render
from django.http import HttpResponse
from updateApp.models import Version

# Create your views here.
def check_android(request):
    if request.method == "GET":
        vs = request.GET.get('vs')
        vs_last = Version.objects.get(num_type=1)
        
	return HttpResponse(vs_last.num)

def check_ios(request):
    if request.method == "GET":
        vs = request.GET.get('vs')
        vs_last = Version.objects.get(num_type=2)
	return HttpResponse(vs_last.num)