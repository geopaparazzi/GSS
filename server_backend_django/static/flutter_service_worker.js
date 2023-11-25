'use strict';
const MANIFEST = 'flutter-app-manifest';
const TEMP = 'flutter-temp-cache';
const CACHE_NAME = 'flutter-app-cache';

const RESOURCES = {"assets/packages/material_design_icons_flutter/lib/fonts/materialdesignicons-webfont.ttf": "3759b2f7a51e83c64a58cfe07b96a8ee",
"assets/packages/smashlibs/assets/tags.json": "11e33c3e93ef68bbc95288124a6bbda0",
"assets/packages/smashlibs/assets/smash_icon.png": "5747fb7b8598d34c6f5144f0f1e703b7",
"assets/packages/smashlibs/assets/smash_text.png": "d03a16169967bdc0637b6f14d3885775",
"assets/packages/smashlibs/assets/maptools_icon.png": "277d78d36d4103fa26fc8d2c5c6ae2bc",
"assets/packages/smashlibs/assets/defaultrender.xml": "11b239fc3e5eb24a0a9a99120cbde05e",
"assets/packages/smashlibs/assets/emptytile256.png": "7543fbc8db296c9aaa0871ac047647b8",
"assets/packages/smashlibs/assets/smash_logo_64.png": "8ec6c3cd8b93b2ea6fbdb779398faa3f",
"assets/packages/smashlibs/assets/fonts/OpenSans-Italic.ttf": "f6238deb7f40a7a03134c11fb63ad387",
"assets/packages/smashlibs/assets/fonts/OpenSans-Regular.ttf": "3ed9575dcc488c3e3a5bd66620bdf5a4",
"assets/packages/smashlibs/assets/fonts/OpenSans-Bold.ttf": "1025a6e0fb0fa86f17f57cc82a6b9756",
"assets/packages/smashlibs/assets/fonts/OpenSans-BoldItalic.ttf": "3a8113737b373d5bccd6f71d91408d16",
"assets/packages/wakelock_plus/assets/no_sleep.js": "7748a45cd593f33280669b29c2c8919a",
"assets/packages/cupertino_icons/assets/CupertinoIcons.ttf": "f2163b9d4e6f1ea52063f498c8878bb9",
"assets/packages/flutter_map/lib/assets/flutter_map_logo.png": "208d63cc917af9713fc9572bd5c09362",
"assets/packages/mapsforge_flutter/assets/patterns/swamp.svg": "711a82e93716a693f73e83a881db8192",
"assets/packages/mapsforge_flutter/assets/patterns/pike.png": "7e2f85abe0b9e36bdad2014ec9319253",
"assets/packages/mapsforge_flutter/assets/patterns/access-private.png": "f74194e09ac16b13ceff28ea7801c4f6",
"assets/packages/mapsforge_flutter/assets/patterns/grass.svg": "3a01f41841676052531631130ab2bb1c",
"assets/packages/mapsforge_flutter/assets/patterns/deciduous.svg": "b3704d99e3533ffbf3096ab07cf86b34",
"assets/packages/mapsforge_flutter/assets/patterns/coniferous.svg": "18e7264ec5075974905c733008591cdd",
"assets/packages/mapsforge_flutter/assets/patterns/access-destination.png": "44b19a1e44b3a0bae55abafd4753fb7a",
"assets/packages/mapsforge_flutter/assets/patterns/quarry.svg": "b195e9d0c32012fceaa9bebe1b23527e",
"assets/packages/mapsforge_flutter/assets/patterns/wood-deciduous.png": "2e055cf34e9fbfac48d6062d5b400492",
"assets/packages/mapsforge_flutter/assets/patterns/scrub.svg": "76895c818ba9bd52d8cf076c1da1ffc3",
"assets/packages/mapsforge_flutter/assets/patterns/nature-reserve.png": "ccd825707607528a3452cf79dd5a48c1",
"assets/packages/mapsforge_flutter/assets/patterns/wood-mixed.png": "0edd5faa12c176d9b909523e6b574fac",
"assets/packages/mapsforge_flutter/assets/patterns/rail.png": "686e6ab1258714b8f088d328e5243bd4",
"assets/packages/mapsforge_flutter/assets/patterns/cemetery.png": "339463b0e2e5c702ebb7fbb9371221ce",
"assets/packages/mapsforge_flutter/assets/patterns/military.png": "9009e0876859c953ab1a332f5eac0f99",
"assets/packages/mapsforge_flutter/assets/patterns/coniferous_and_deciduous.svg": "6876aafd40f082a3a1b8597c89341d07",
"assets/packages/mapsforge_flutter/assets/patterns/dot.png": "3fdd075a7fee668beedfd963c932d53c",
"assets/packages/mapsforge_flutter/assets/patterns/farmland.svg": "47f5cfde05648771bb4543279e117b4a",
"assets/packages/mapsforge_flutter/assets/patterns/marsh.png": "bed03b5d23ecd45232747e165038203c",
"assets/packages/mapsforge_flutter/assets/patterns/hills.svg": "69ac70ca45ba91cc71f8f66ded069aa2",
"assets/packages/mapsforge_flutter/assets/patterns/wood-coniferous.png": "b27564f7f1d462f951b3b4fcd763d32d",
"assets/packages/mapsforge_flutter/assets/patterns/arrow.png": "803caccd6d0b55209830d855230db005",
"assets/packages/mapsforge_flutter/assets/symbols/water/weir.svg": "3cbd351314f9186fef90dce5d9db648f",
"assets/packages/mapsforge_flutter/assets/symbols/peak.svg": "f3339427d54f8190aede3c9f7f2cb4e4",
"assets/packages/mapsforge_flutter/assets/symbols/transport/parking.svg": "6840c5f50066d27268e7de87a1985ad4",
"assets/packages/mapsforge_flutter/assets/symbols/transport/slipway.svg": "04c828996e4f2eaeb3a67d394fd3a833",
"assets/packages/mapsforge_flutter/assets/symbols/transport/rental_bicycle.svg": "c05e90996d23165e0d558adbbcf2fceb",
"assets/packages/mapsforge_flutter/assets/symbols/transport/tram_stop.svg": "35ac18e4b18a3fef72facb89cad9b282",
"assets/packages/mapsforge_flutter/assets/symbols/transport/parking_private.svg": "e0d377745b38850d7a99f48b2fba5981",
"assets/packages/mapsforge_flutter/assets/symbols/transport/traffic_lights.svg": "16305fe14d7565cf54d9a918d039af5b",
"assets/packages/mapsforge_flutter/assets/symbols/transport/bus_station.svg": "d427f7ee70a5febac52ce7d88bc821cd",
"assets/packages/mapsforge_flutter/assets/symbols/transport/lighthouse.svg": "89f83d9553dd1f1c0d8ae14ed3866e1f",
"assets/packages/mapsforge_flutter/assets/symbols/transport/airport2.svg": "b681cc477fe90f66f35071284761880d",
"assets/packages/mapsforge_flutter/assets/symbols/transport/train_station2.svg": "7c0a26517136aa483fb039bed9b8b92d",
"assets/packages/mapsforge_flutter/assets/symbols/transport/helicopter.svg": "323428d5235088518981a2958d3ffd05",
"assets/packages/mapsforge_flutter/assets/symbols/transport/bus_stop.svg": "02788609629563864ed7bccb99ad0c11",
"assets/packages/mapsforge_flutter/assets/symbols/transport/fuel.svg": "8571b189ed3aad4a39f87156276d9259",
"assets/packages/mapsforge_flutter/assets/symbols/gondola.svg": "85d6db63e4866397f4b33d1fe7281d29",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/picnic.svg": "3766daaf0dab86fad451469d89473638",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/information.svg": "3814be183e85b5b5ed8c574e19bfda53",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/attraction.svg": "2ba162fc4ef4c1e3847b46e81053351c",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/zoo.svg": "6106d0e0ce35ecbc5ee3a4f05ef86f00",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/museum.svg": "09d6b4aa2d4372c8d0eb1722990be5dd",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/theatre.svg": "b9c5f9daf67e6931e18f9d91122ffb9d",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/ruin.svg": "9c0d36cac27d8f3bd424c0ab36c7793b",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/monument.svg": "6748f6dda5f0c3c38325d00f23a03829",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/castle2.svg": "a11f887583c72772a89e40c30337ddf7",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/view_point.svg": "3f85d93ed4eba6dcef0e197cac6e7bf4",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/memorial.svg": "64e32032fb06cc308b3d4a86d5d9b175",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/archaeological2.svg": "062ff537589c728a46987541b9a1729d",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/windmill.svg": "508eb50ba09d5625e562bee030c3798b",
"assets/packages/mapsforge_flutter/assets/symbols/tourist/cinema2.svg": "57d20abaf71c542d744236e042d9ca77",
"assets/packages/mapsforge_flutter/assets/symbols/education/university.svg": "04b26f0602b5df4ac29d594cd394eb50",
"assets/packages/mapsforge_flutter/assets/symbols/education/nursery3.svg": "a53f0691d78f890e39964fcee21c35cd",
"assets/packages/mapsforge_flutter/assets/symbols/education/school.svg": "a814967f96d8062cc828fd19ad55f573",
"assets/packages/mapsforge_flutter/assets/symbols/dot_white.svg": "89381645541e4c60dc122f702ce92150",
"assets/packages/mapsforge_flutter/assets/symbols/accommodation/hostel.svg": "fa93b49c59a5087da2f8684cbbfdc698",
"assets/packages/mapsforge_flutter/assets/symbols/accommodation/hotel2.svg": "1e77211d7deae4552c6a16c2629307b0",
"assets/packages/mapsforge_flutter/assets/symbols/accommodation/alpinehut.svg": "674b311479a6eb82d0f33a2543b8f21e",
"assets/packages/mapsforge_flutter/assets/symbols/accommodation/camping.svg": "0cf01fb5ef99153fac24af7a85b78d45",
"assets/packages/mapsforge_flutter/assets/symbols/accommodation/shelter2.svg": "01d861ac7ddc3da48c16700be3896e47",
"assets/packages/mapsforge_flutter/assets/symbols/accommodation/chalet.svg": "e5eaabe2c0a67f3117064ca903ee8cdb",
"assets/packages/mapsforge_flutter/assets/symbols/accommodation/caravan_park.svg": "55debaca17c1323ab513527fef9267ea",
"assets/packages/mapsforge_flutter/assets/symbols/dot_black.svg": "1886237fbb894a1ebcc87cb02e566851",
"assets/packages/mapsforge_flutter/assets/symbols/food/fastfood.svg": "9c1d8a3385fb49b7866e202c992cc8aa",
"assets/packages/mapsforge_flutter/assets/symbols/food/drinkingtap.svg": "7c0d26c5806dd05c2447b769b41cc989",
"assets/packages/mapsforge_flutter/assets/symbols/food/biergarten.svg": "043db4a1915d87098eadb4c20ce74545",
"assets/packages/mapsforge_flutter/assets/symbols/food/restaurant.svg": "5bfee2a4342d26f1b0c67c1386a7bba2",
"assets/packages/mapsforge_flutter/assets/symbols/food/pub.svg": "ee4b817b37d8fdbbba9fece1b5d32ccb",
"assets/packages/mapsforge_flutter/assets/symbols/food/cafe.svg": "96edf6ab971dcd2ba7d6b3e4a081b4e3",
"assets/packages/mapsforge_flutter/assets/symbols/food/bar.svg": "3627509ca77bc759d15229db1cedf751",
"assets/packages/mapsforge_flutter/assets/symbols/bench.svg": "11cb348489f0be0f3aa78de28b5113f7",
"assets/packages/mapsforge_flutter/assets/symbols/traffic_signal.svg": "3e2fa1a87a677f785107b9d8309f5bbb",
"assets/packages/mapsforge_flutter/assets/symbols/chair_lift.svg": "d90f4d5c02c0976f3eca73ade33e5721",
"assets/packages/mapsforge_flutter/assets/symbols/sport/tennis.svg": "75aeabde2ef3b2b9e4b8c8b16d6ab48a",
"assets/packages/mapsforge_flutter/assets/symbols/sport/soccer.svg": "fcca1ecfaa4e7d2417f7fb513b815abf",
"assets/packages/mapsforge_flutter/assets/symbols/sport/shooting.svg": "18dbe447658d97c305079297ddf5ac64",
"assets/packages/mapsforge_flutter/assets/symbols/sport/golf.svg": "d5c3c06b0b230b22ecc74c69eef2d605",
"assets/packages/mapsforge_flutter/assets/symbols/sport/stadium.svg": "6fbbf938af74625c2db5c2ecd18d95bf",
"assets/packages/mapsforge_flutter/assets/symbols/sport/swimming_outdoor.svg": "80002b4290a2a798e26f0206ad505461",
"assets/packages/mapsforge_flutter/assets/symbols/amenity/post_office.svg": "23099546749d1b169732d71d9e737513",
"assets/packages/mapsforge_flutter/assets/symbols/amenity/telephone.svg": "013d3fb5afc6face5ed8f39d157c109d",
"assets/packages/mapsforge_flutter/assets/symbols/amenity/recycling.svg": "099949630248b332ddd5103e849d084e",
"assets/packages/mapsforge_flutter/assets/symbols/amenity/playground.svg": "daa862a26e61bda61d8802c5b6b1e77e",
"assets/packages/mapsforge_flutter/assets/symbols/amenity/toilets.svg": "1c2eac83724f4270648781224de95725",
"assets/packages/mapsforge_flutter/assets/symbols/amenity/fountain2.svg": "e1f347e7443d40070f4e2813a24521e5",
"assets/packages/mapsforge_flutter/assets/symbols/amenity/post_box.svg": "1f1aab05da96b78b5e5663e92d111231",
"assets/packages/mapsforge_flutter/assets/symbols/amenity/firestation3.svg": "024b75d3501f0282c766dd2be04d931e",
"assets/packages/mapsforge_flutter/assets/symbols/amenity/library.svg": "2d10724e3f1f0f2419593cd0bdd38be9",
"assets/packages/mapsforge_flutter/assets/symbols/amenity/police2.svg": "c104571197f5ca357461a9f741dabd8d",
"assets/packages/mapsforge_flutter/assets/symbols/railway-crossing-small.svg": "398b6c2f3c589bb9ecab8cc4fede2e66",
"assets/packages/mapsforge_flutter/assets/symbols/viewpoint.svg": "ae7bbbde292cb75aedd20e62ff622421",
"assets/packages/mapsforge_flutter/assets/symbols/shopping/car_repair.svg": "a9d7fb389cd319a49c50639b8fd03d9d",
"assets/packages/mapsforge_flutter/assets/symbols/shopping/butcher.svg": "3d6324b176ade063ce7013cc797b00a3",
"assets/packages/mapsforge_flutter/assets/symbols/shopping/book.svg": "548f4829f8d8f9a2745fd11dc42a712f",
"assets/packages/mapsforge_flutter/assets/symbols/shopping/diy.svg": "0762a186cf2aaef7236acd750485318e",
"assets/packages/mapsforge_flutter/assets/symbols/shopping/convenience.svg": "d957034797666734dfa42db41d9c6d07",
"assets/packages/mapsforge_flutter/assets/symbols/shopping/bakery.svg": "1185c2b6c4879b5717e7655de5e3cf41",
"assets/packages/mapsforge_flutter/assets/symbols/shopping/greengrocer.svg": "5567361b752a828d24fdda22d26b7235",
"assets/packages/mapsforge_flutter/assets/symbols/shopping/alcohol.svg": "9fcb5e688dd26f3a840717baf4744b98",
"assets/packages/mapsforge_flutter/assets/symbols/shopping/bicycle.svg": "d2b2aed9d3634d4884dbb4c8d5caa03a",
"assets/packages/mapsforge_flutter/assets/symbols/shopping/supermarket.svg": "ea8f808e7ab946af6056a30bd7db1acf",
"assets/packages/mapsforge_flutter/assets/symbols/shopping/marketplace.svg": "f82ded2ce888a629a77bfd6f5f7283b8",
"assets/packages/mapsforge_flutter/assets/symbols/shopping/laundrette.svg": "49a0da2c6e5e4c2c118b4855e56d2484",
"assets/packages/mapsforge_flutter/assets/symbols/poi/embassy2.svg": "00a939c42a41684ee12d4596772f43c5",
"assets/packages/mapsforge_flutter/assets/symbols/poi/mountain_pass.svg": "8a00e189b83e850ca6f9c66262d34c48",
"assets/packages/mapsforge_flutter/assets/symbols/poi/cave.svg": "391ea427cd171f587ef1808d53f97803",
"assets/packages/mapsforge_flutter/assets/symbols/money/bank2.svg": "9adbfa13838e7eec24ab256ffb4a9f0a",
"assets/packages/mapsforge_flutter/assets/symbols/money/atm2.svg": "eac4dd515bdfb749197d1b38e4865ab6",
"assets/packages/mapsforge_flutter/assets/symbols/dot_blue_dark.svg": "9df39deaeb4e7231bbd8fa1f50c91fbf",
"assets/packages/mapsforge_flutter/assets/symbols/place_of_worship/hindu.svg": "73aff4171dadd10bb7da5390c67f816a",
"assets/packages/mapsforge_flutter/assets/symbols/place_of_worship/christian.svg": "1399c9f3268a7a8b285e4f26581bd1c1",
"assets/packages/mapsforge_flutter/assets/symbols/place_of_worship/shinto.svg": "903d7cf7ad12bc297aa68f23f69425f2",
"assets/packages/mapsforge_flutter/assets/symbols/place_of_worship/jewish.svg": "498513adc45d04cd0e18c3ac0ee7e634",
"assets/packages/mapsforge_flutter/assets/symbols/place_of_worship/islamic.svg": "d8b2cf891188dabdea740081302cb336",
"assets/packages/mapsforge_flutter/assets/symbols/place_of_worship/unknown.svg": "497f7661b6ec3232b7981e39db8b1711",
"assets/packages/mapsforge_flutter/assets/symbols/place_of_worship/buddhist.svg": "4bb19fdaf28cfaaf6a5bac0d5f8d4dd7",
"assets/packages/mapsforge_flutter/assets/symbols/oneway.svg": "67fa1359d70a69558afb3c601fd90ae1",
"assets/packages/mapsforge_flutter/assets/symbols/windsock.svg": "1a96a9d5d084bd247449d1a7bbf20d9a",
"assets/packages/mapsforge_flutter/assets/symbols/railway-crossing.svg": "7c3d3576b27b6dfa4327d4b51b6bb610",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/information.svg": "0d1bfaff3918f8462ee79d6f1dc6b035",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/attraction.svg": "72180009f00c695b8e536fdbaeb93347",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/zoo.svg": "8a59e688c97d205b0bc70f46c1b40972",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/museum.svg": "1f8dbb7a5f37024af1a5733d049558bd",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/theatre.svg": "2d8fa9c3beea72ad6c2d3ef5ee80d25c",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/ruin.svg": "f2c59bbddd6554a1df5d2d9870c3281f",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/monument.svg": "e5d5a884fcfde6124ec0ce920fce04e3",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/castle2.svg": "efc482eecd57caae884bf6a45cf1f0a2",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/view_point.svg": "454515084adab87b1de39f4b27a49c3c",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/memorial.svg": "76bb18652d6fbbdb16c809264f311564",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/archaeological2.svg": "a628747e83c67272cc7d5fa16f61cbdc",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/windmill.svg": "d0ea19554c4b68fef540d3a20e4e2dde",
"assets/packages/mapsforge_flutter/assets/symbols/custom/tourist/cinema2.svg": "bf5306455b62632deb2610863ed6e3a8",
"assets/packages/mapsforge_flutter/assets/symbols/custom/amenity/toilets.svg": "a5a27266989e1569615f67aa37057ae8",
"assets/packages/mapsforge_flutter/assets/symbols/custom/amenity/fountain2.svg": "10df41c8fa8ca68250c4d86967135d7c",
"assets/packages/mapsforge_flutter/assets/symbols/custom/amenity/library.svg": "aea3162ba53604649be9c68f8d1aa1be",
"assets/packages/mapsforge_flutter/assets/symbols/custom/money/atm2.svg": "5400ba55e122c5698ca2ac4d661fdc74",
"assets/packages/mapsforge_flutter/assets/symbols/dot_magenta.svg": "e9ae4c6c2ae43b9d5c2647741dc16c08",
"assets/packages/mapsforge_flutter/assets/symbols/cable_car.svg": "f0e684eab61cdf133e821d0c516d101e",
"assets/packages/mapsforge_flutter/assets/symbols/dot_blue.svg": "93d881f3ebfb8bcc7d500b6eb5b0c369",
"assets/packages/mapsforge_flutter/assets/symbols/volcano.svg": "4e727b31556780090ba0a7be40ed082d",
"assets/packages/mapsforge_flutter/assets/symbols/barrier/cycle_barrier.svg": "55ffb97dbd526ebb55dbabe691655732",
"assets/packages/mapsforge_flutter/assets/symbols/barrier/stile.svg": "9be0086ac3709bac0ddfba6c50b11d68",
"assets/packages/mapsforge_flutter/assets/symbols/barrier/blocks.svg": "6fb8554ee4ff1d3ffc12d4b50cfc1ad5",
"assets/packages/mapsforge_flutter/assets/symbols/barrier/bollard.svg": "84914111c5f50a020f29e62c171f6719",
"assets/packages/mapsforge_flutter/assets/symbols/barrier/gate.svg": "26ffb784376d6157cda6a0cd525ef463",
"assets/packages/mapsforge_flutter/assets/symbols/barrier/lift_gate.svg": "80502b4b1c9806d62fc2d958ede4cc1c",
"assets/packages/mapsforge_flutter/assets/symbols/health/doctors2.svg": "4d8f7ad9d270c7051dbf84f6cd256ab2",
"assets/packages/mapsforge_flutter/assets/symbols/health/pharmacy.svg": "1629d51bd0dc197054ad82d55bd88035",
"assets/packages/mapsforge_flutter/assets/symbols/health/hospital.svg": "59ea09bd7401ea268d55f757bc5cd06f",
"assets/packages/golden_toolkit/fonts/Roboto-Regular.ttf": "ac3f799d5bbaf5196fab15ab8de8431c",
"assets/assets/smash_logo.png": "27190564dadcb7fb76ba4d2ba0ef6bee",
"assets/AssetManifest.bin.json": "6d2ecd9f30a825c4b343b33133ce10ee",
"assets/NOTICES": "3a46866ca443d32d6c8404dbbacf522c",
"assets/AssetManifest.bin": "84dd184f06b0c85bf55de64fb3ef6b18",
"assets/AssetManifest.json": "a055e88f0ee485a40a327910064ef799",
"assets/shaders/ink_sparkle.frag": "4096b5150bac93c41cbc9b45276bd90f",
"assets/FontManifest.json": "10772e85b042c1c326b000efaef85924",
"assets/fonts/MaterialIcons-Regular.otf": "8ffeceb2c68f3c6d707448034510b534",
"loading.png": "13a7bcbcf5b0261a8c12e9ed516bd733",
"canvaskit/skwasm.wasm": "4124c42a73efa7eb886d3400a1ed7a06",
"canvaskit/skwasm.js": "87063acf45c5e1ab9565dcf06b0c18b8",
"canvaskit/chromium/canvaskit.wasm": "f87e541501c96012c252942b6b75d1ea",
"canvaskit/chromium/canvaskit.js": "0ae8bbcc58155679458a0f7a00f66873",
"canvaskit/canvaskit.wasm": "64edb91684bdb3b879812ba2e48dd487",
"canvaskit/skwasm.worker.js": "bfb704a6c714a75da9ef320991e88b03",
"canvaskit/canvaskit.js": "eb8797020acdbdf96a12fb0405582c1b",
"favicon.png": "e2724f96681cd245c4e428a41f4aebf0",
"manifest.json": "5ca15113382a9571f815e4fb444dd4c5",
"version.json": "d7124c999c6ca2b53ee4660c32447f2e",
"index.html": "7a93c7be6e04f0bd458029533dd891f2",
"/": "7a93c7be6e04f0bd458029533dd891f2",
"main.dart.js": "c998d35e3d59725832c00261a37e53c0",
"flutter.js": "7d69e653079438abfbb24b82a655b0a4"};
// The application shell files that are downloaded before a service worker can
// start.
const CORE = ["main.dart.js",
"index.html",
"assets/AssetManifest.json",
"assets/FontManifest.json"];

// During install, the TEMP cache is populated with the application shell files.
self.addEventListener("install", (event) => {
  self.skipWaiting();
  return event.waitUntil(
    caches.open(TEMP).then((cache) => {
      return cache.addAll(
        CORE.map((value) => new Request(value, {'cache': 'reload'})));
    })
  );
});
// During activate, the cache is populated with the temp files downloaded in
// install. If this service worker is upgrading from one with a saved
// MANIFEST, then use this to retain unchanged resource files.
self.addEventListener("activate", function(event) {
  return event.waitUntil(async function() {
    try {
      var contentCache = await caches.open(CACHE_NAME);
      var tempCache = await caches.open(TEMP);
      var manifestCache = await caches.open(MANIFEST);
      var manifest = await manifestCache.match('manifest');
      // When there is no prior manifest, clear the entire cache.
      if (!manifest) {
        await caches.delete(CACHE_NAME);
        contentCache = await caches.open(CACHE_NAME);
        for (var request of await tempCache.keys()) {
          var response = await tempCache.match(request);
          await contentCache.put(request, response);
        }
        await caches.delete(TEMP);
        // Save the manifest to make future upgrades efficient.
        await manifestCache.put('manifest', new Response(JSON.stringify(RESOURCES)));
        // Claim client to enable caching on first launch
        self.clients.claim();
        return;
      }
      var oldManifest = await manifest.json();
      var origin = self.location.origin;
      for (var request of await contentCache.keys()) {
        var key = request.url.substring(origin.length + 1);
        if (key == "") {
          key = "/";
        }
        // If a resource from the old manifest is not in the new cache, or if
        // the MD5 sum has changed, delete it. Otherwise the resource is left
        // in the cache and can be reused by the new service worker.
        if (!RESOURCES[key] || RESOURCES[key] != oldManifest[key]) {
          await contentCache.delete(request);
        }
      }
      // Populate the cache with the app shell TEMP files, potentially overwriting
      // cache files preserved above.
      for (var request of await tempCache.keys()) {
        var response = await tempCache.match(request);
        await contentCache.put(request, response);
      }
      await caches.delete(TEMP);
      // Save the manifest to make future upgrades efficient.
      await manifestCache.put('manifest', new Response(JSON.stringify(RESOURCES)));
      // Claim client to enable caching on first launch
      self.clients.claim();
      return;
    } catch (err) {
      // On an unhandled exception the state of the cache cannot be guaranteed.
      console.error('Failed to upgrade service worker: ' + err);
      await caches.delete(CACHE_NAME);
      await caches.delete(TEMP);
      await caches.delete(MANIFEST);
    }
  }());
});
// The fetch handler redirects requests for RESOURCE files to the service
// worker cache.
self.addEventListener("fetch", (event) => {
  if (event.request.method !== 'GET') {
    return;
  }
  var origin = self.location.origin;
  var key = event.request.url.substring(origin.length + 1);
  // Redirect URLs to the index.html
  if (key.indexOf('?v=') != -1) {
    key = key.split('?v=')[0];
  }
  if (event.request.url == origin || event.request.url.startsWith(origin + '/#') || key == '') {
    key = '/';
  }
  // If the URL is not the RESOURCE list then return to signal that the
  // browser should take over.
  if (!RESOURCES[key]) {
    return;
  }
  // If the URL is the index.html, perform an online-first request.
  if (key == '/') {
    return onlineFirst(event);
  }
  event.respondWith(caches.open(CACHE_NAME)
    .then((cache) =>  {
      return cache.match(event.request).then((response) => {
        // Either respond with the cached resource, or perform a fetch and
        // lazily populate the cache only if the resource was successfully fetched.
        return response || fetch(event.request).then((response) => {
          if (response && Boolean(response.ok)) {
            cache.put(event.request, response.clone());
          }
          return response;
        });
      })
    })
  );
});
self.addEventListener('message', (event) => {
  // SkipWaiting can be used to immediately activate a waiting service worker.
  // This will also require a page refresh triggered by the main worker.
  if (event.data === 'skipWaiting') {
    self.skipWaiting();
    return;
  }
  if (event.data === 'downloadOffline') {
    downloadOffline();
    return;
  }
});
// Download offline will check the RESOURCES for all files not in the cache
// and populate them.
async function downloadOffline() {
  var resources = [];
  var contentCache = await caches.open(CACHE_NAME);
  var currentContent = {};
  for (var request of await contentCache.keys()) {
    var key = request.url.substring(origin.length + 1);
    if (key == "") {
      key = "/";
    }
    currentContent[key] = true;
  }
  for (var resourceKey of Object.keys(RESOURCES)) {
    if (!currentContent[resourceKey]) {
      resources.push(resourceKey);
    }
  }
  return contentCache.addAll(resources);
}
// Attempt to download the resource online before falling back to
// the offline cache.
function onlineFirst(event) {
  return event.respondWith(
    fetch(event.request).then((response) => {
      return caches.open(CACHE_NAME).then((cache) => {
        cache.put(event.request, response.clone());
        return response;
      });
    }).catch((error) => {
      return caches.open(CACHE_NAME).then((cache) => {
        return cache.match(event.request).then((response) => {
          if (response != null) {
            return response;
          }
          throw error;
        });
      });
    })
  );
}
