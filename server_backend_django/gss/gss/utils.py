from datetime import datetime

class Utilities():

    PATTERN_WITH_SECONDS = "%Y-%m-%d %H:%M:%S"
    PATTERN_COMPACT = "%Y%m%d_%H%M%S"

    @staticmethod
    def collectImageIds(dataMap, idsList):
        for key in dataMap.keys():
            value = dataMap[key]
            if isinstance(value, dict):
                Utilities.collectImageIds(value, idsList)
            elif isinstance(value, list):
                for item in value:
                    Utilities.collectImageIds(item, idsList)
            else:
                if value == 'pictures':
                    id = dataMap["value"]
                    if len(id.strip()) > 0:
                        tmpIds = str(id).split(";")
                        for tmpId in tmpIds:
                            idsList.append(int(tmpId))

    @staticmethod
    def updateImageIds(dataMap, old2NewIdsMap):
        for key in dataMap.keys():
            value = dataMap[key]
            if isinstance(value, dict):
                Utilities.updateImageIds(value, old2NewIdsMap)
            elif isinstance(value, list):
                for item in value:
                    Utilities.updateImageIds(item, old2NewIdsMap)
            else:
                if value == 'pictures':
                    id = dataMap["value"]
                    if len(id.strip()) > 0:
                        previousIds = str(id).split(";")
                        newIds = []
                        for previousId in previousIds:
                            newId = old2NewIdsMap[int(previousId)]
                            newIds.append(str(newId))
                        
                        dataMap["value"] = ";".join(newIds)

    
    @staticmethod
    def collectIsLabelValue(dataMap, labelList):
        if len(labelList) > 0:
            return
        for key in dataMap.keys():
            value = dataMap[key]
            if isinstance(value, dict):
                Utilities.collectIsLabelValue(value, labelList)
            elif isinstance(value, list):
                if key == 'formitems':
                    formitemsList = value
                    for item in formitemsList:
                        if 'islabel' in item:
                            isLabel = item['islabel']
                            if isLabel == "true":
                                label = item['value']
                                labelList.append(label)
                for item in value:
                    Utilities.collectIsLabelValue(item, labelList)
            else:
                if key == 'formitems':
                    formitemsList = value
                    for item in formitemsList:
                        if 'islabel' in item:
                            isLabel = item['islabel']
                            if isLabel == "true":
                                label = item['value']
                                labelList.append(label)



    @staticmethod
    def newDatetime(string:str = None) -> datetime:
        """Create a new datetime object.

        :param string: if supplied, it is used to build the datetime, else now is returned.
        :return: the datetime object.
        """
        if string:
            return datetime.strptime(string, Utilities.PATTERN_WITH_SECONDS)
        else:
            return datetime.now()
        
    @staticmethod
    def newDatetimeUtc(string:str = None) -> datetime:
        """Create a new UTC datetime object.

        :param string: if supplied, it is used to build the datetime, else now is returned.
        :return: the datetime object.
        """
        if string:
            return datetime.strptime(string, Utilities.PATTERN_WITH_SECONDS).replace(tzinfo=datetime.timezone.utc)
        else:
            return datetime.utcnow()
        
    @staticmethod
    def toStringWithSeconds( dt:datetime ) -> str:
        """Get String of format: YYYY-MM-DD HH:MM:SS from a datetime object.

        :param dt: the datetime object to format.
        :return: the formatted string.
        """
        return dt.strftime(Utilities.PATTERN_WITH_SECONDS)
    
    @staticmethod
    def toStringWithMinutes( dt:datetime ) -> str:
        """Get String of format: YYYY-MM-DD HH:MM from a datetime object.

        :param dt: the datetime object to format.
        :return: the formatted string.
        """
        return dt.strftime("%Y-%m-%d %H:%M")
    
    @staticmethod
    def toStringCompact( dt:datetime ) -> str:
        """Get String of format: YYYYMMDD_HHMMSS from a datetime object.

        :param dt: the datetime object to format.
        :return: the formatted string.
        """
        return dt.strftime(Utilities.PATTERN_COMPACT)
    
    @staticmethod
    def quickUtcToString( unixEpoch:int ) -> str:
        """Quick long to timestamp string formatter, UTC.

        :param unixEpoch: the unix epoch to convert.
        :return: the timestamp string as yyyy-MM-dd HH:mm:ss
        """
        dt = datetime.fromtimestamp(unixEpoch, datetime.timezone.utc)

        return dt.strftime(Utilities.PATTERN_WITH_SECONDS)
    
    @staticmethod
    def quickToString( unixEpoch:int ) -> str:
        """Quick long to timestamp string formatter.

        :param unixEpoch: the unix epoch to convert.
        :return: the timestamp string as yyyy-MM-dd HH:mm:ss
        """
        dt = datetime.fromtimestamp(unixEpoch)

        return dt.strftime(Utilities.PATTERN_WITH_SECONDS)
    
    @staticmethod
    def toEpochInMillis( dt:datetime ) -> int:
        """Get millis since epoch from date object.
        
        :param dt: the datetime object.
        :return: the millis since epoch.
        """
        return dt.timestamp() * 1000