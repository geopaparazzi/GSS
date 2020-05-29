import 'package:flutter_map/flutter_map.dart';
import 'package:flutter_server/com/hydrologis/gss/network.dart';

const MAPSFORGE = "Mapsforge";

final AVAILABLE_MAPS = {
  '$MAPSFORGE': TileLayerOptions(
    tms: false,
    urlTemplate: '$WEBAPP_URL/tiles/mapsforge/{z}/{x}/{y}',
    tileProvider: NonCachingNetworkTileProvider(),
  ),
  'Openstreetmap': TileLayerOptions(
    tms: false,
    subdomains: const ['a', 'b', 'c'],
    maxZoom: 19,
    urlTemplate: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
    tileProvider: NonCachingNetworkTileProvider(),
  ),
  'OpenTopoMap': TileLayerOptions(
    tms: false,
    maxZoom: 19,
    subdomains: const ['a', 'b', 'c'],
    urlTemplate: "https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png",
    tileProvider: NonCachingNetworkTileProvider(),
  ),
  'Stamen Watercolor': TileLayerOptions(
    tms: false,
    maxZoom: 19,
    urlTemplate: "http://c.tile.stamen.com/watercolor/{z}/{x}/{y}.jpg",
    tileProvider: NonCachingNetworkTileProvider(),
  ),
  'Wikimedia Map': TileLayerOptions(
    tms: false,
    maxZoom: 19,
    urlTemplate: "https://maps.wikimedia.org/osm-intl/{z}/{x}/{y}.png",
    tileProvider: NonCachingNetworkTileProvider(),
  ),
};
