from rest_framework.permissions import BasePermission, IsAdminUser
from data.models import DbNamings

"""
Base permission classes for GSS system users/groups
"""

class IsSuperUser(IsAdminUser):
    def has_permission(self, request, view):
        return request.user and request.user.is_superuser


class IsCoordinator(BasePermission):
    def has_permission(self, request, view):
        superuser = bool(request.user and request.user.is_superuser)
        if superuser:
            return True
        return DbNamings.GROUP_COORDINATORS in request.user.groups.values_list("name", flat=True)


class IsSurveyor(BasePermission):
    def has_permission(self, request, view):
        superuser = bool(request.user and request.user.is_superuser)
        if superuser:
            return True
        isWebuserCheck = DbNamings.GROUP_SURVEYORS in request.user.groups.values_list("name", flat=True)
        return isWebuserCheck

class IsFormbuilder(BasePermission):
    def has_permission(self, request, view):
        superuser = bool(request.user and request.user.is_superuser)
        if superuser:
            return True
        isWebuserCheck = DbNamings.GROUP_FORMBUILDERS in request.user.groups.values_list("name", flat=True)
        return isWebuserCheck


class IsWebuser(BasePermission):
    def has_permission(self, request, view):
        superuser = bool(request.user and request.user.is_superuser)
        if superuser:
            return True

        isWebuserCheck = DbNamings.GROUP_WEBUSERS in request.user.groups.values_list("name", flat=True)
        return isWebuserCheck
