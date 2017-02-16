from django.shortcuts import render

# Create your views here.
def index(request):
    return render(request, 'h5_mobile_mymain.html', {})
    

