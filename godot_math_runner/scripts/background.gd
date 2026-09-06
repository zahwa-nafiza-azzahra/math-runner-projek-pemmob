extends Node2D

# background.gd
# Manages endless track scrolling, obstacle and coin spawning.

@export var scroll_speed: float = 380.0

@onready var bg_sprite1: Sprite2D = $Background1
@onready var bg_sprite2: Sprite2D = $Background2
@onready var items_container: Node2D = $ItemsContainer

var coin_scene = preload("res://scenes/coin.tscn")
var obstacle_scene = preload("res://scenes/obstacle.tscn")

var bg_height: float = 1920.0
var run_timer: float = 0.0
var question_encounter_interval: float = 4.0 # Runs for 4 sec then encounters question

func _ready() -> void:
	bg_sprite1.position = Vector2(540, 960)
	bg_sprite2.position = Vector2(540, -960)
	spawn_initial_track_items()

func _process(delta: float) -> void:
	if GameManager.current_state != GameManager.GameState.RUNNING:
		return

	# Scroll backgrounds downwards
	bg_sprite1.position.y += scroll_speed * delta
	bg_sprite2.position.y += scroll_speed * delta

	if bg_sprite1.position.y >= 1920.0 + 960.0:
		bg_sprite1.position.y = bg_sprite2.position.y - 1920.0

	if bg_sprite2.position.y >= 1920.0 + 960.0:
		bg_sprite2.position.y = bg_sprite1.position.y - 1920.0

	# Check question encounter timer
	run_timer += delta
	if run_timer >= question_encounter_interval:
		run_timer = 0.0
		# Trigger Question Encounter & Stop Runner!
		QuestionManager.generate_question()
		GameManager.set_state(GameManager.GameState.QUESTION_ENCOUNTER)

func spawn_initial_track_items() -> void:
	for i in range(4):
		var y_pos = 400.0 + float(i) * 300.0
		var lane = randi_range(-1, 1)
		spawn_coin_at(lane, y_pos)

func spawn_coin_at(lane: int, y_pos: float) -> void:
	var coin = coin_scene.instantiate()
	coin.position = Vector2(540.0 + float(lane) * 220.0, y_pos)
	items_container.add_child(coin)

func clear_all_items() -> void:
	for child in items_container.get_children():
		child.queue_free()
