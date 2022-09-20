from datetime import datetime
from rest_framework import serializers
from .models import Note

class NoteSerializer(serializers.ModelSerializer):
    def to_internal_value(self, data):
        data = data.copy()
        
        # Convert JavaScript UNIX timestamp to ISO 8601
        ts_parsed = datetime.fromtimestamp(int(data['ts'])//1000)
        data['ts'] = datetime.isoformat(ts_parsed)
        # Make PROJECT_NAME lowercase
        if 'PROJECT_NAME' in data:
            data['project_name'] = data['PROJECT_NAME']
            del data['PROJECT_NAME']
        # Ignore type (only notes are supported)
        del data['type']
        # IDs are not supported
        del data['_id']
        
        return data
    
    class Meta:
        model = Note
        fields = '__all__'
