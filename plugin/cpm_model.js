(function() {
	var import_action, export_action;

	Plugin.register('cpm_model', {
		title: 'CPM Model Format',
		icon: 'star',
		author: 'Gamepiaynmo',
		description: 'Import and export models of Minecraft mod Custom Player Model (CPM).',
		version: '0.1.0',
		variant: 'desktop',

		onload() {
			var codec = new Codec('cpm_model', {
				name: 'CPM Model',
				extension: 'json',
				remember: false,
				compile(options) {
					var bones = []
					var texture = null
					if (textures[0]) {
						texture = "tex." + textures[0].name.substring(0, textures[0].name.indexOf("."))
						var textureSize = [ Project.texture_width, Project.texture_height ]
					}

					function processGroup(group, parent) {
						var bone = { "id" : group.name }
						if (parent)
							bone["parent"] = parent.name
						bone["position"] = group.origin
						bone["rotation"] = group.rotation
						if (texture) {
							bone["texture"] = texture
							bone["textureSize"] = textureSize
						}

						var boxes = []
						for (var child of group.children) {
							if (child instanceof Cube) {
								boxes.push(processCube(child, group))
							}
						}

						bone["boxes"] = boxes
						bones.push(bone)

						for (var child of group.children) {
							if (child instanceof Group) {
								processGroup(child, group)
							}
						}
					}

					function processCube(cube, parent) {
						var from = cube.from
						var to = cube.to

						var box = {
							"textureOffset": cube.uv_offset,
							"coordinates": [ from[0], from[1], from[2], to[0] - from[0], to[1] - from[1], to[2] - from[2]]
						}

						return box
					}

					Outliner.root.forEach(obj => {
						if (obj instanceof Group) {
							processGroup(obj)
						}
					})

					var model = { "bones": bones }
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

			MenuBar.addAction(import_action, 'file.import')
			MenuBar.addAction(export_action, 'file.export')
		},

		onunload() {
			import_action.delete()
			export_action.delete()
		}
	});

})()
