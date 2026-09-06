extends Area2D
class_name TrackObstacle

# obstacle.gd
# Handles road obstacles (Red Block X, Green Block ÷, Spike Obstacle).

enum ObstacleType {
	RED_BLOCK,
	GREEN_BLOCK,
	SPIKE
}

@export var obstacle_type: ObstacleType = ObstacleType.SPIKE
@export var move_speed: float = 380.0

@onready var sprite: Sprite2D = $Sprite2D
var has_hit_player: bool = false

func _ready() -> void:
	body_entered.connect(_on_body_entered)
	update_texture()

func update_texture() -> void:
	match obstacle_type:
		ObstacleType.RED_BLOCK:
			sprite.texture = load("res://assets/sprites/obstacles/obstacle_block_red.png")
		ObstacleType.GREEN_BLOCK:
			sprite.texture = load("res://assets/sprites/obstacles/obstacle_block_green.png")
		ObstacleType.SPIKE:
			sprite.texture = load("res://assets/sprites/obstacles/obstacle_spike.png")

func _process(delta: float) -> void:
	if GameManager.current_state != GameManager.GameState.RUNNING:
		return
	position.y += move_speed * delta

	if position.y > 2100.0:
		queue_free()

func _on_body_entered(body: Node2D) -> void:
	if has_hit_player or not (body is MathRunnerPlayer):
		return
	has_hit_player = true
	ScoreManager.apply_wrong_answer_penalty()
	(body as MathRunnerPlayer).play_animation(MathRunnerPlayer.AnimState.WRONG)

	if ScoreManager.current_lives <= 0:
		GameManager.set_state(GameManager.GameState.GAME_OVER)
