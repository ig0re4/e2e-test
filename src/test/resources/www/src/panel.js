function Panel(tree, socket) {
	this.socket = socket;
	that = this;
	$("#create").click(function(){
		that.create();
		return false;
	});
	$("#remove").click(function(){
		that.remove();
		return false;
	});
	$("#update").click(function(){
		that.update();
		return false;
	});
	
}

Panel.prototype.show = function(json) {
	if((json !== undefined) && (json.data !== undefined)){
		if(json.data.title !== undefined){
			$("#name").val(json.data.title);
		}else{
			$("#name").val("");
		}

		if(json.data.value !== undefined){
			$("#value").val(json.data.value);
			$("#value").prop("readonly",false);
		}else{
			$("#value").val("");
			$("#value").prop("readonly",true);
		}
		
		if(json.data.path !== undefined){
			$("#path").val(json.data.path);
		}else{
			$("#path").val("");
		}
		
		if(json.data.file !== undefined){
			$("#file").val(json.data.file);
		}else{
			$("#file").val("");
		}
		
		if(json.data.isInherited !== undefined){
			$("#isInherited").val(json.data.isInherited);
		}else{
			$("#isInherited").val("");
		}
		
		if(json.data.type !== undefined){
			$("#type").val(json.data.type);
		}else{
			$("#type").val("UNDEFINED");
		}
	}
};

Panel.prototype.update = function(){
	var object4Send = {
		"command": "update",
		"type": this.getType(),
		"value": $("#value").val(),
		"id":$("#path").val()
	}
	var object4Update = {
		"title": $("#name").val(),
		"value": $("#value").val(),
		"type": this.getType(),
		"path":$("#path").val(), 
		"isInherited":$("#isInherited").val(), 
		"file":$("#file").val()
	}
	this.socket.update(JSON.stringify(object4Send), object4Update);
};

Panel.prototype.create = function() {
	var object4Send = {
		"command": "create",
		"type": this.getType(),
		"value": $("#value").val(),
		"id":$("#path").val() + "/" + $("#name").val()
	}
	var object4Add = {
		"title": $("#name").val(),
		"value": $("#value").val(),
		"type": this.getType(),
		"path":$("#path").val(), 
		"isInherited":$("#isInherited").val(), 
		"file":$("#file").val()
	}
	this.socket.create(JSON.stringify(object4Send), object4Add);
}

Panel.prototype.remove = function() {
	var object = {
		"command": "delete",
		"id":$("#path").val()
	}
	this.socket.remove(JSON.stringify(object));
}

Panel.prototype.getType = function() {
	var type = $("#type").val();
	if(type === "UNDEFINED"){
		return "STRING"
	}
	return type;
}

