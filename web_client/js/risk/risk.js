var colors = ['cyan', 'green', 'blue', 'red', 'purple', 'pink'];
var Risk = {

	/**
	 * Settings Object, holding application wide settings
	 */
	GlobalScale: function(){
		return ($("#map").width() / 1696.0);
	},

	Settings :{
		colors: {cyan: '#00ffe4', green: '#0f0', blue: '#00f', red: '#f00', purple: '#f0f', pink: '##ffcccc'}
	},

	Territories: {},
	stage: null,
	mapLayer: null,
	topLayer:  null,
	backgroundLayer: null,

	init: function() {
		//Initiate our main Territories Object, it contains essential data about the territories current state
		Risk.setUpTerritoriesObj();

		//Initiate a Kinetic stage
		Risk.stage = new Kinetic.Stage({
			container: 'map', //1696/1080
			width: $("#map").width(),
            height: $("#map").width() * 1080/1696
		});

		Risk.mapLayer = new Kinetic.Layer({
			scale: Risk.GlobalScale()
		});

		Risk.topLayer = new Kinetic.Layer({
			scale: Risk.GlobalScale()
		});

		Risk.drawBackgroundImg();
		Risk.drawTerritories();

		Risk.stage.add(Risk.backgroundLayer);
		Risk.stage.add(Risk.mapLayer);
		Risk.stage.add(Risk.topLayer);

		Risk.mapLayer.draw();

		Risk.setTerritoriesColour();
	},

	/**
	 * Initiate the  Risk.Territories Object, this will contain essential informations about the territories
	 */
	setUpTerritoriesObj: function() {
		for(id in TerritoryNames) {

			var pathObject = new Kinetic.Path({
				data: TerritoryPathData[id].path,
				id: id //set a unique id --> path.attrs.id
			});

			//Using a sprite image for territory names
			//see: drawImage() -- https://developer.mozilla.org/en-US/docs/Canvas_tutorial/Using_images , and see Kinetic.Image() docs for more
			var sprite = new Image();
			sprite.src = 'resources/names.png';
			var territoryNameImg = new Kinetic.Image({
				image: sprite,
				x: FontDestinationCoords[id].x,
				y: FontDestinationCoords[id].y,
				width: FontSpriteCoords[id].sWidth, //'destiantion Width'
				height: FontSpriteCoords[id].sHeight, //'destination Height'
				crop: [FontSpriteCoords[id].sx, FontSpriteCoords[id].sy, FontSpriteCoords[id].sWidth, FontSpriteCoords[id].sHeight]

			});

			var game_state_terrority = "not_found";
			for(game_state_country_id in game_state.map.countries){
				var country = game_state.map.countries[game_state_country_id];
				if(country.name.toLowerCase().replace(" ", "") == TerritoryNames[id].toLowerCase().replace(" ", "")){
					game_state_terrority = country;
				}
			}
			if(game_state_terrority === "not_found"){
				console.log("Mapping for " + TerritoryNames[id] + " not found");
			}

			Risk.Territories[id] = {
				name: TerritoryNames[id],
				path: pathObject,
				nameImg: territoryNameImg,
				color: null,
				neighbours: Neighbours[id],
				armyNum: null,
				mapped_game_state_territory : game_state_terrority
			};
		}

	},

	drawBackgroundImg: function() {
		Risk.backgroundLayer = new Kinetic.Layer({
			scale: Risk.GlobalScale()
		});
		var imgObj = new Image();
		imgObj.src = 'resources/map_grey_new.jpg';
		
		var img = new Kinetic.Image({
			image: imgObj
			//alpha: 0.8
		});
		Risk.backgroundLayer.add(img);
	},

	updateMap: function(){
		$("#map").html(""); //clear old displayed map
		Risk.Territories = {};

		Risk.setUpTerritoriesObj();

		//Initiate a Kinetic stage
		Risk.stage = new Kinetic.Stage({
			container: 'map', //1696/1080
			width: $("#map").width(),
			height: $("#map").width() * 1080/1696
		});

		Risk.mapLayer = new Kinetic.Layer({
			scale: Risk.GlobalScale()
		});

		Risk.topLayer = new Kinetic.Layer({
			scale: Risk.GlobalScale()
		});

		//Risk.drawBackgroundImg();
		Risk.drawTerritories();

		Risk.stage.add(Risk.backgroundLayer);
		Risk.stage.add(Risk.mapLayer);
		Risk.stage.add(Risk.topLayer);

		Risk.mapLayer.draw();
		Risk.setTerritoriesColour();
		Risk.setTerritoriesColour();

	},


	drawTerritories: function() {
		for (t in Risk.Territories) {
			
			var path = Risk.Territories[t].path;
			var nameImg = Risk.Territories[t].nameImg;
			var group = new Kinetic.Group();

			//We have to set up a group for proper mouseover on territories and sprite name images 
			group.add(path);
			group.add(nameImg);
			Risk.mapLayer.add(group);
		
			//Basic animations 
			//Wrap the 'path', 't' and 'group' variables inside a closure, and set up the mouseover / mouseout events for the demo
			//when you make a bigger application you should move this functionality out from here, and maybe put these 'actions' in a seperate function/'class'
			(function(path, t, group) {
				group.on('mouseover', function() {
					path.setFill('#eee');
					path.setOpacity(0.3);
					group.moveTo(Risk.topLayer);
					Risk.topLayer.drawScene();
				});

				group.on('mouseout', function() {
					path.setFill(Risk.Settings.colors[Risk.Territories[t].color]);
					path.setOpacity(0.4);
					group.moveTo(Risk.mapLayer);
					Risk.topLayer.draw();
				});

				group.on('click', function() {
					console.log(Risk.Territories[path.attrs.id]);
					//location.hash = path.attrs.id;
				});
			})(path, t, group);
		}				
	},

	setTerritoriesColour: function() {
		Risk.mapLayer.draw();
			for(var id in Risk.Territories) {
				var color = colors[Risk.Territories[id].mapped_game_state_territory.player_owner_id];
				Risk.Territories[id].color = color;
				Risk.Territories[id].path.setFill(Risk.Settings.colors[color]);
				Risk.Territories[id].path.setOpacity(0.4);
			}
	}
};