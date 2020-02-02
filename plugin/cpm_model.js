(function() {
	let import_action, export_action;
	let options = {};

	Plugin.register('cpm_model', {
		title: 'CPM Model Format',
		icon: 'star',
		author: 'Gamepiaynmo',
		description: 'Import and export models of Minecraft mod Custom Player Model (CPM).',
		version: '0.1.1',
		variant: 'desktop',

		onload() {
			let codec = new Codec('cpm_model', {
				name: 'CPM Model',
				extension: 'json',
				remember: false,
				compile() {
					let bones = [];
					let texture = "texturename";
					let filename = "modelname";
					if (textures[0]) {
						let index = textures[0].name.indexOf(".");
						if (index >= 0) {
							filename = textures[0].name.substring(0, index);
							texture = "tex." + filename;
							let textureSize = [ Project.texture_width, Project.texture_height ];
						}
					}

					let skeleton = {};
					let deg2rad = Math.PI / 180;
					let rad2deg = 180 / Math.PI;
					if (options.scale !== 1) {
						var scale = [options.scale, options.scale, options.scale];
						var invScale = [1 / options.scale, 1 / options.scale, 1 / options.scale];
					}

					let baseBones = {};
					function setSkeleton(parent, bone, group) {
						let bones = parent + "_all";
						let pos = [group.origin[0] * options.scale, group.origin[1] * options.scale - 24, group.origin[2] * options.scale];
						skeleton[bones] = pos;
						baseBones[bones] = bone;
					}

					function getParent(group, parent) {
						if (parent.name) return parent.name;
						if (group.name == "head_c") return "head";
						if (group.name == "body_c") return "body";
						if (group.name == "left_arm_c") return "left_arm";
						if (group.name == "right_arm_c") return "right_arm";
						if (group.name == "left_leg_c") return "left_leg";
						if (group.name == "right_leg_c") return "right_leg";
						return null;
					}

					let body_bone = null;
					function processAttachment(bone, group) {
						if (bone.id == "head_c") {
							if (scale) bone.scale = scale;
							setSkeleton("head", bone, group);
							if (options.helmet || options.hat) {
								let helmet = {
									id: bone.id + "_helmet_hat",
									parent: bone.id,
									attached: [],
								};
								if (options.helmet) helmet.attached.push("helmet_all");
								if (options.hat) helmet.attached.push("head_wearing");
								if (invScale) helmet.scale = invScale;
								bones.push(helmet);
							}
						} else if (bone.id == "body_c") {
							if (scale) bone.scale = scale;
							setSkeleton("body", bone, group);
							if (options.chestplate || options.cape || options.elytra || options.leggings) {
								let chestplate = {
									id: bone.id + "_chestplate_body",
									parent: bone.id,
									attached: [],
								};
								if (options.chestplate) {
									if (options.leggings) chestplate.attached.push("armor_body_all");
									else chestplate.attached.push("chestplate_body");
								} else if (options.leggings) chestplate.attached.push("leggings_body");
								if (invScale) chestplate.scale = invScale;
								body_bone = chestplate;
								bones.push(chestplate);
								let cape = {
									id: bone.id + "_cape_elytra",
									parent: chestplate.id,
									attached: [],
									texture: "tex.skin",
								}
								if (options.cape) cape.attached.push("cape");
								if (options.elytra) cape.attached.push("elytra");
								bones.push(cape);
							}

							if (options.parrots) {
								let left = {
									id: bone.id + "_parrot_left",
									parent: bone.id,
									attached: ["shoulder_parrot_left"],
									position: [-6.5, 10, 0],
								};
								let right = {
									id: bone.id + "_parrot_right",
									parent: bone.id,
									attached: ["shoulder_parrot_right"],
									position: [6.5, 10, 0],
								};
								if (invScale) {
									left.scale = invScale;
									right.scale = invScale;
								}
								bones.push(left, right);
							}
						} else if (bone.id == "left_arm_c") {
							if (scale) bone.scale = scale;
							setSkeleton("left_arm", bone, group);
							if (options.chestplate) {
								let sleeve = {
									id: bone.id + "_chestplate_left",
									parent: bone.id,
									attached: ["chestplate_left_arm"],
									position: [0, -2, 0],
								};
								if (invScale) sleeve.scale = invScale;
								bones.push(sleeve);
							}

							if (options.items) {
								let item = {
									id: bone.id + "_item_left",
									parent: bone.id,
									attached: ["held_item_left"],
									position: [-1, -12, -1],
								};
								if (invScale) item.scale = invScale;
								bones.push(item);
							}
						} else if (bone.id == "right_arm_c") {
							if (scale) bone.scale = scale;
							setSkeleton("right_arm", bone, group);
							if (options.chestplate) {
								let sleeve = {
									id: bone.id + "_chestplate_right",
									parent: bone.id,
									attached: ["chestplate_right_arm"],
									position: [0, -2, 0],
								};
								if (invScale) sleeve.scale = invScale;
								bones.push(sleeve);
							}

							if (options.items) {
								let item = {
									id: bone.id + "_item_right",
									parent: bone.id,
									attached: ["held_item_right"],
									position: [1, -12, -1],
								};
								if (invScale) item.scale = invScale;
								bones.push(item);
							}
						} else if (bone.id == "left_leg_c") {
							if (scale) bone.scale = scale;
							setSkeleton("left_leg", bone, group);
							if (options.leggings || options.boots) {
								let legging = {
									id: bone.id + "_legging_left",
									parent: bone.id,
									attached: [],
								};
								if (options.leggings) {
									if (options.boots) legging.attached.push("armor_left_leg_all");
									else legging.attached.push("leggings_left_leg");
								} else if (options.boots) legging.attached.push("boots_left_leg");
								if (invScale) legging.scale = invScale;
								bones.push(legging);
							}
						} else if (bone.id == "right_leg_c") {
							if (scale) bone.scale = scale;
							setSkeleton("right_leg", bone, group);
							if (options.leggings || options.boots) {
								let legging = {
									id: bone.id + "_legging_right",
									parent: bone.id,
									attached: [],
								};
								if (options.leggings) {
									if (options.boots) legging.attached.push("armor_right_leg_all");
									else legging.attached.push("leggings_right_leg");
								} else if (options.boots) legging.attached.push("boots_right_leg");
								if (invScale) legging.scale = invScale;
								bones.push(legging);
							}
						} else {
							if (scale) bone.scale = scale;
						}
					}

					function getChild(group) {
						for (let child of group.children) {
							if (child instanceof Group) {
								return child;
							}
						}
					}

					function getChildrenCount(group) {
						let res = 0;
						for (let child of group.children) {
							if (child instanceof Group) {
								res += 1;
							}
						}
						return res;
					}

					function processGroup(group, parent, parent_root) {
						let bone = { id: group.name };
						let parent_name = getParent(group, parent);
						if (parent_name) bone.parent = parent_name;
						if (!parent.name) bone.texture = texture;
						let root = parent.name == null && parent_name != null;

						if (getChildrenCount(parent) > 1 || parent_root) {
							let pos = [group.origin[0] - parent.origin[0],
								group.origin[1] - parent.origin[1],
								group.origin[2] - parent.origin[2]];
							if (!root && (pos[0] != 0 || pos[1] != 0 || pos[2] != 0)) {
								let dummy_name = group.name + "_cpm_dummy";
								let dummy = { id: dummy_name };
								bones.push(dummy);

								bone.parent = dummy_name;
								if (parent_name) dummy.parent = parent_name;
								dummy.position = pos;
							}
						}
						bones.push(bone);
						if (root) processAttachment(bone, group);

						let child_cnt = getChildrenCount(group);
						if (child_cnt == 1) {
							let child = getChild(group);
							let pos = [child.origin[0] - group.origin[0],
								child.origin[1] - group.origin[1],
								child.origin[2] - group.origin[2]];
							if (!root && (pos[0] != 0 || pos[1] != 0 || pos[2] != 0)) {
								bone.position = pos;
							}
						}

						let rot = group.rotation;
						if (rot[0] != 0 || rot[1] != 0 || rot[2] != 0)
							bone.rotation = [-rot[1], -rot[0], rot[2]];

						let boxes = [];
						for (let child of group.children) {
							if (child instanceof Cube) {
								boxes.push(processCube(child, group.origin, bone.position));
							}
						}
						bone.boxes = boxes;

						for (let child of group.children) {
							if (child instanceof Group) {
								processGroup(child, group, root);
							}
						}
					}

					function processCube(cube, pos, orin) {
						let from = cube.from;
						let to = cube.to;
						if (!orin)
							orin = [0, 0, 0];

						let box = {
							textureOffset: cube.uv_offset,
							coordinates: [from[0] - pos[0] - orin[0], from[1] - pos[1] - orin[1], to[2] - pos[2] - orin[2],
								Math.max(1e-3, to[0] - from[0]), Math.max(1e-3, to[1] - from[1]), Math.max(1e-3, to[2] - from[2])]
						};

						if (cube.inflate != 0)
							box.sizeAdd = cube.inflate;
						if (cube.mirror_uv)
							box.mirror = true;

						return box;
					}

					let root = {};
					root.children = [];
					root.origin = [0, 24 / options.scale, 0];
					Outliner.root.forEach(obj => {
						if (obj instanceof Group) {
							root.children.push(obj);
						}
					})
					Outliner.root.forEach(obj => {
						if (obj instanceof Group) {
							processGroup(obj, root, false);
						}
					})

					if (skeleton["head_all"] && skeleton["body_all"]) {
						let body_height = skeleton["head_all"][1] - skeleton["body_all"][1];
						if (body_height != 0) {
							if (baseBones["body_all"])
								baseBones["body_all"].position = [0, -body_height / options.scale, 0];
							if (body_bone)
								body_bone.position = [0, body_height, 0];
							skeleton["body_all"][1] = skeleton["head_all"][1];
						}
					}

					if (skeleton["head_all"]) {
						let pos = skeleton["head_all"];
						pos[1] = "if(is_sneaking," + (pos[1] - 1).toFixed(4) + "," + pos[1].toFixed(4) + ")";
					}
					if (skeleton["left_leg_all"]) {
						let pos = skeleton["left_leg_all"];
						pos[2] = "if(is_sneaking," + (4 + pos[2]).toFixed(4) + "," + pos[2].toFixed(4) + ")";
					}
					if (skeleton["right_leg_all"]) {
						let pos = skeleton["right_leg_all"];
						pos[2] = "if(is_sneaking," + (4 + pos[2]).toFixed(4) + "," + pos[2].toFixed(4) + ")";
					}

					let model = {
						modelId: filename,
						modelName: filename,
						hide: ["model_all", "feature_all"],
						skeleton: skeleton,
						bones: bones,
					};

					return autoStringify(model);
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
					let dialog = new Dialog({
						title: "CPM Model Export",
						id: "cpm_export",
						lines: [
							'<p>Scale: <input type="number" id="scale" value=1 style="background-color:var(--color-back)"></p>',
							'<p>Helmet: <input type="checkbox" checked=true id="helmet"></p>',
							'<p>Hat: <input type="checkbox" checked=true id="hat"></p>',
							'<p>Chestplate: <input type="checkbox" checked=true id="chestplate"></p>',
							'<p>Leggings: <input type="checkbox" checked=true id="leggings"></p>',
							'<p>Boots: <input type="checkbox" checked=true id="boots"></p>',
							'<p>Parrots: <input type="checkbox" checked=true id="parrots"></p>',
							'<p>Items: <input type="checkbox" checked=true id="items"></p>',
							'<p>Cape: <input type="checkbox" checked=true id="cape"></p>',
							'<p>Elytra: <input type="checkbox" checked=true id="elytra"></p>',
						],
						draggable: true,
						onConfirm() {
							dialog.hide();
							options.scale = parseFloat($('.dialog#cpm_export input#scale').val());
							options.helmet = $('.dialog#cpm_export input#helmet').is(':checked');
							options.hat = $('.dialog#cpm_export input#hat').is(':checked');
							options.chestplate = $('.dialog#cpm_export input#chestplate').is(':checked');
							options.leggings = $('.dialog#cpm_export input#leggings').is(':checked');
							options.boots = $('.dialog#cpm_export input#boots').is(':checked');
							options.parrots = $('.dialog#cpm_export input#parrots').is(':checked');
							options.items = $('.dialog#cpm_export input#items').is(':checked');
							options.cape = $('.dialog#cpm_export input#cape').is(':checked');
							options.elytra = $('.dialog#cpm_export input#elytra').is(':checked');
							codec.export();
						}
					});
					dialog.show();
				}
			})

			// MenuBar.addAction(import_action, 'file.import')
			MenuBar.addAction(export_action, 'file.export');
		},

		onunload() {
			import_action.delete();
			export_action.delete();
		}
	});

})()
