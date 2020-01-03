(function() {
	var import_action, export_action;

	Plugin.register('cpm_model', {
		title: 'CPM Model Format',
		icon: 'star',
		author: 'Gamepiaynmo',
		description: 'Import and export models of Minecraft mod Custom Player Model (CPM).',
		version: '0.1.1',
		variant: 'desktop',

		onload() {
			var codec = new Codec('cpm_model', {
				name: 'CPM Model',
				extension: 'json',
				remember: false,
				compile(options) {
					var bones = []
					var texture = "texturename"
					var filename = "modelname"
					if (textures[0]) {
						var index = textures[0].name.indexOf(".")
						if (index >= 0) {
							filename = textures[0].name.substring(0, index)
							texture = "tex." + filename
							var textureSize = [ Project.texture_width, Project.texture_height ]
						}
					}

					var deg2rad = Math.PI / 180
					var rad2deg = 180 / Math.PI

					function getParent(group, parent) {
						if (parent.name) { return parent.name }
						if (group.name == "head_c") { return "head" }
						if (group.name == "body_c") { return "body" }
						if (group.name == "left_arm_c") { return "left_arm" }
						if (group.name == "right_arm_c") { return "right_arm" }
						if (group.name == "left_leg_c") { return "left_leg" }
						if (group.name == "right_leg_c") { return "right_leg" }
						return null
					}

					function getChild(group) {
						for (var child of group.children) {
							if (child instanceof Group) {
								return child
							}
						}
					}

					function getChildrenCount(group) {
						var res = 0
						for (var child of group.children) {
							if (child instanceof Group) {
								res += 1
							}
						}
						return res
					}

					function processGroup(group, parent) {
						var bone = { "id": group.name }
						var parent_name = getParent(group, parent)
						if (parent_name) { bone["parent"] = parent_name }
						if (!parent.name) { bone["texture"] = texture }
						var root = parent.name && parent_name

						if (getChildrenCount(parent) > 1) {
							var pos = [group.origin[0] - parent.origin[0],
								group.origin[1] - parent.origin[1],
								group.origin[2] - parent.origin[2]]
							if (root && (pos[0] != 0 || pos[1] != 0 || pos[2] != 0)) {
								var dummy_name = group.name + "_cpm_dummy"
								var dummy = { "id": dummy_name }
								bones.push(dummy)

								bone["parent"] = dummy_name
								if (parent_name) { dummy["parent"] = parent_name }
								dummy["position"] = pos
							}
						}
						bones.push(bone)

						var child_cnt = getChildrenCount(group)
						if (child_cnt == 1) {
							var child = getChild(group)
							var pos = [child.origin[0] - group.origin[0],
								child.origin[1] - group.origin[1],
								child.origin[2] - group.origin[2]]
							if (root && (pos[0] != 0 || pos[1] != 0 || pos[2] != 0)) {
								bone["position"] = pos
							}
						}

						var rot = group.rotation
						if (rot[0] != 0 || rot[1] != 0 || rot[2] != 0) {
							bone["rotation"] = [-rot[1], -rot[0], rot[2]]
						}

						var boxes = []
						for (var child of group.children) {
							if (child instanceof Cube) {
								boxes.push(processCube(child, group.origin, bone["position"]))
							}
						}
						bone["boxes"] = boxes

						for (var child of group.children) {
							if (child instanceof Group) {
								processGroup(child, group)
							}
						}
					}

					function processCube(cube, pos, orin) {
						var from = cube.from
						var to = cube.to
						if (!orin)
							orin = [0, 0, 0]

						var box = {
							"textureOffset": cube.uv_offset,
							"coordinates": [from[0] - pos[0] - orin[0], from[1] - pos[1] - orin[1], to[2] - pos[2] - orin[2],
								to[0] - from[0], to[1] - from[1], to[2] - from[2]]
						}

						if (cube.inflate != 0) {
							box["sizeAdd"] = cube.inflate
						}
						if (cube.mirror_uv) {
							box["mirror"] = true
						}

						return box
					}

					var root = {}
					root.children = []
					root.origin = [0, 12, 0]
					Outliner.root.forEach(obj => {
						if (obj instanceof Group) {
							root.children.push(obj)
						}
					})
					Outliner.root.forEach(obj => {
						if (obj instanceof Group) {
							processGroup(obj, root)
						}
					})

					var model = {
						"modelId": filename,
						"modelName": filename,
						"hide": ["model_all"],
						"bones": bones
					}
					return autoStringify(model)
				},
				parse(model, path) {

				}
			})

			import_action = new Action('import_cpm_model', {
				name: 'Import CPM Model',
				description: '',
				icon: 'star',
				category: 'file',
				click() {
					ElecDialogs.showOpenDialog(
					    currentwindow,
					    {
					        title: 'Import CPM Mpdel',
					        dontAddToRecent: true,
					        filters: [{
					            name: '',
					            extensions: ['json']
					        }]
					    },
					function (files) {
					    if (!files) return
					    fs.readFile(files[0], (err, data) => {
					        if (err) return
					        codec.parse(data, files[0])
					    });
					});
				}
			})

			export_action = new Action('export_cpm_model', {
				name: 'Export CPM Model',
				description: '',
				icon: 'star',
				category: 'file',
				click() {
					codec.export();
				}
			})

			// MenuBar.addAction(import_action, 'file.import')
			MenuBar.addAction(export_action, 'file.export')
		},

		onunload() {
			import_action.delete()
			export_action.delete()
		}
	});

})()
