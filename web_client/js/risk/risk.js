var colors = ['cyan', 'green', 'blue', 'red', 'purple', 'pink'];
var Risk = {

	/**
	 * Settings Object, holding application wide settings
	 */
	GlobalScale: function(){
		return ($("#map").width() / 1696.0);
	},

	Settings :{
		colors: {cyan: '#00ffe4', green: '#0f0', blue: '#00f', red: '#f00', purple: '#f0f', pink: '#ffcccc'}
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
				armyPoint: ArmyPoints[id],
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
        Risk.stage.destroy();
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

		Risk.drawBackgroundImg();
		Risk.drawTerritories();

		Risk.stage.add(Risk.backgroundLayer);
		Risk.stage.add(Risk.mapLayer);
		Risk.stage.add(Risk.topLayer);

		Risk.mapLayer.draw();
		Risk.setTerritoriesColour();
		Risk.setTerritoriesColour();

	},

	showArmyInfo: function(territory){

			var layer = new Kinetic.Layer();
            var armyCount = territory.mapped_game_state_territory.troop_count;
            if(armyCount == -1) armyCount = "";
			var armyNumText = new Kinetic.Text({
				x: (territory.armyPoint.x) * 1,
				y: (territory.armyPoint.y) * 1,
				//        text: 'COMPLEX TEXT\n\nAll the world\'s a stage, and all the men and women merely players. They have their exits and their entrances.',
				text: armyCount,
				fontSize: 30,
				fontFamily: 'Calibri',
				fill: '#555',
				width: 30,
				padding: 0,
				align: 'center'
			});

			var cicle = new Kinetic.Circle({
				x: (territory.armyPoint.x) * 1 + 13,
				y: (territory.armyPoint.y) * 1 + 10,
				stroke: '#555',
				strokeWidth: 1,
				fill: '#ddd',
				radius: (armyCount == "") ? 0 : 20,
				shadowColor: 'black',
				shadowBlur: 1,
				shadowOffset: [1, 1],
				shadowOpacity: 0.2,
				cornerRadius: 2
			});

			// add the shapes to the layer
			//layer.add(simpleText);
			var group = new Kinetic.Group();
			group.add(cicle);
			group.add(armyNumText);
		return group;
			//Risk.mapLayer.add(cicle);
			//Risk.mapLayer.add(armyNumText);
			//Risk.stage.add(layer);
	},

	drawTerritories: function() {
		for (t in Risk.Territories) {
			
			var path = Risk.Territories[t].path;
			var nameImg = Risk.Territories[t].nameImg;
			var group = new Kinetic.Group();

			//We have to set up a group for proper mouseover on territories and sprite name images
			group.add(path);
			group.add(nameImg);
			group.add(Risk.showArmyInfo(Risk.Territories[t]));
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
                    Risk.updateMap();
				});

				group.on('click', function() {
					console.log(Risk.Territories[path.attrs.id]);
                    if(game_state.currentPlayer.ID == my_player_id){
                        var selectedCountry = Risk.Territories[path.attrs.id].mapped_game_state_territory;
                        switch (game_state.turn_stage){
                            case "STAGE_SETUP":
                                claimCountryDuringSetup(selectedCountry.country_id);
                                break;
                            case "STAGE_DEPLOYING":
                                if($.inArray(selectedCountry,selectedTerriories) == -1)
                                    selectedTerriories.push(selectedCountry);
                                updateTurnPanel();
                                break;
                            case "STAGE_BATTLES":
                                if(selectedCountry.player_owner_id == my_player_id){
                                    attackOrigin = selectedCountry;
                                    attackDestination = null;
                                    updateTurnPanel();
                                }else if(attackOrigin != null){
                                    //get neighbours of origin
                                    var neighboursOfOrigin, selectedTerritoryKey;
                                    for(var t in Risk.Territories){
                                        var territory = Risk.Territories[t];
                                        if(Risk.Territories[t].mapped_game_state_territory.country_id == attackOrigin.country_id)
                                            neighboursOfOrigin = Risk.Territories[t].neighbours;

                                        if(Risk.Territories[t].mapped_game_state_territory.name == selectedCountry.name)
                                            selectedTerritoryKey = t;
                                    }
                                    if($.inArray(selectedTerritoryKey, neighboursOfOrigin) > -1) {
                                        attackDestination = selectedCountry;
                                        updateTurnPanel();
                                    }else{
                                        attackDestination = null;
                                        updateTurnPanel();
                                        $("#attackOutcome").html('<div class="alert alert-warning alert-dismissible" role="alert">' +
                                        '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                                        "Warning! You can only attack a territory neighbouring " + attackOrigin.name + "." +
                                        '</div>');
                                    }
                                }
                                break;
                            case "STAGE_FORTIFY":
                                if(selectedCountry.player_owner_id == my_player_id){
                                    if(fortificationOrigin == null) {
                                        if(selectedCountry.troop_count != 1) {
                                            fortificationOrigin = selectedCountry;
                                            updateTurnPanel();
                                        }else{
                                            $("#fortifyStatus").html('<div class="alert alert-warning alert-dismissible" role="alert">' +
                                            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                                            "Error! You must fortify from a territory with at least 2 armies</div>");
                                        }
                                    }else{
                                        if(fortificationDestination == null){

                                            //get neighbours of origin
                                            for(var t in Risk.Territories){
                                                var territory = Risk.Territories[t];
                                                if(Risk.Territories[t].mapped_game_state_territory.country_id == fortificationOrigin.country_id)
                                                    neighboursOfOrigin = Risk.Territories[t].neighbours;

                                                if(Risk.Territories[t].mapped_game_state_territory.name == selectedCountry.name)
                                                    selectedTerritoryKey = t;
                                            }
                                            if($.inArray(selectedTerritoryKey, neighboursOfOrigin) > -1) {
                                                fortificationDestination = selectedCountry;
                                                updateTurnPanel();
                                            }else{
                                                $("#fortifyStatus").html('<div class="alert alert-warning alert-dismissible" role="alert">' +
                                                '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                                                "Warning! You can only fortify to a territory neighbouring " + fortificationOrigin.name + "." +
                                                '</div>');
                                            }




                                        }
                                    }
                                }


                                break;//fortificationOrigin
                        }
                    }

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
