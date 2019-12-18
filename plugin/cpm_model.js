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

					var deg2rad = Math.PI / 180
					var rad2deg = 180 / Math.PI

					function getTransform(group, parent, parentMatrix) {
						cl(group.name)
						var euler = new THREE.Euler(-group.rotation[1] * deg2rad,
							-group.rotation[0] * deg2rad, group.rotation[2] * deg2rad)
						cl(euler)
						var matrix = new THREE.Matrix4()
						matrix.makeRotationFromEuler(euler)
						var trans = new THREE.Vector3(group.origin[0] - parent.origin[0],
							group.origin[1] - parent.origin[1],
							group.origin[2] - parent.origin[2])
						cl(trans)
						matrix.setPosition(trans)
						var result = new THREE.Matrix4()
						return result.multiplyMatrices(parentMatrix, matrix)
					}

					function decomposeTransform(matrix) {
						cl(matrix)
						var position = new THREE.Vector3()
						var quaternion = new THREE.Quaternion()
						var scale = new THREE.Vector3
						matrix.decompose(position, quaternion, scale)
						cl(position)
						cl(euler)
						cl(scale)
						var euler = new THREE.Euler()
						euler.setFromQuaternion(quaternion)
						return [position, euler]
					}

					function processGroup(group, parent) {
						var bone = { "id" : group.name }

						var pos = group.origin
						if (parent) {
							bone["parent"] = parent.name
							var piv = parent.origin
							bone["position"] = [pos[0] - piv[0], pos[1] - piv[1], pos[2] - piv[2]]
							var rot = parent.rotation
							bone["rotation"] = [-rot[1], -rot[0], rot[2]]
						} else {
							bone["position"] = [pos[0], pos[1] - 12, pos[2]]
							if (texture) {
								bone["texture"] = texture
								bone["textureSize"] = textureSize
							}
						}

						bones.push(bone)

						var boxes = []
						for (var child of group.children) {
							if (child instanceof Cube) {
								boxes.push(processCube(child, group.origin))
							}
						}

						if (boxes.length > 0) {
							var rot = group.rotation
							var dummy = { "id": group.name + "_cpm_dummy",
								"parent": group.name,
								"rotation": [-rot[1], -rot[0], rot[2]],
								"boxes": boxes
							}

							bones.push(dummy)
						}

						for (var child of group.children) {
							if (child instanceof Group) {
								processGroup(child, group)
							}
						}
					}

					function processCube(cube, pos) {
						var from = cube.from
						var to = cube.to

						var box = {
							"textureOffset": cube.uv_offset,
							"coordinates": [from[0] - pos[0], from[1] - pos[1], to[2] - pos[2],
								to[0] - from[0], to[1] - from[1], to[2] - from[2]]
						}

						return box
					}

					Outliner.root.forEach(obj => {
						if (obj instanceof Group) {
							processGroup(obj)
						}
					})

					var model = {
						"modelId": "aaa",
						"modelName": "aaa",
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

			MenuBar.addAction(import_action, 'file.import')
			MenuBar.addAction(export_action, 'file.export')
		},

		onunload() {
			import_action.delete()
			export_action.delete()
		}
	});

})()
