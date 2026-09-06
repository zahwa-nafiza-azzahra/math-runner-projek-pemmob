extends Area2D
class_name CoinItem

# coin.gd
# Collectible coin that awards points and particle effects.

@export var move_speed: float = 380.0

@onready var sprite: Sprite2D = $Sprite2D
var is_collected: bool = false

func _ready() -> void:
	body_entered.connect(_on_body_entered)
	# Gentle rotating bob
	var t = create_tween().set_loops()
	t.tween_property(sprite, "scale:x", 0.7, 0.3)
	t.tween_property(sprite, "scale:x", 1.0, 0.3)

func _process(delta: float) -> void:
	if GameManager.current_state != GameManager.GameState.RUNNING:
		return

	position.y += move_speed * delta

	if position.y > 2100.0:
		# Recycle back to top
		position.y = -200.0
		position.x = 540.0 + float(randi_range(-1, 1)) * 220.0
		is_collected = false
		visible = true

func _on_body_entered(body: Node2D) -> void:
	if is_collected or not (body is MathRunnerPlayer):
		return
	is_collected = true
	ScoreManager.add_coin(1)
	(body as MathRunnerPlayer).play_animation(MathRunnerPlayer.AnimState.COLLECT)

	# Collection animation
	var t = create_tween()
	t.tween_property(self, "scale", Vector2(1.6, 1.6), 0.15)
	t.tween_property(self, "modulate:a", 0.0, 0.15)
	t.finished.connect(func(): visible = false)
