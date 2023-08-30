from django.urls import path
from . import views

urlpatterns = [
    path('layers/', views.layers, name='layers'),
    path('data/<str:form_name>', views.DataListView.as_view(), name='data-list'),
    path('data/<str:form_name>/<int:form_id>', views.DataDetailView.as_view(), name='data-detail'),
]