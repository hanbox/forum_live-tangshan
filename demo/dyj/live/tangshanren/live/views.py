from django.shortcuts import render
from .models import user_session
from django.http import HttpResponse

# Create your views here.
def index(request):
    live_session = user_session.objects.all()
    return render(request, 'h5_mobile_live_main.html', {'live_session':live_session})
    
def iwantlive(request):
    return render(request, 'h5_mobile_live_wantlive.html', {})

def register(request):
    if request.method == "POST":
        room = user_session()
        roomname = request.POST['roomname']
        cover = request.FILES['image']
        room.title = roomname
        room.playCount = 998
        room.cover = cover
        room.save()

        return HttpResponse('upload ok!')
    else:
        return render(request, 'h5_mobile_live_wantlive.html',{})