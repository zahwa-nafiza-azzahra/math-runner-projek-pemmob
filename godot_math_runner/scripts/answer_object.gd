extends Area2D
class_name AnswerObject

# answer_object.gd
# Visual platform in track that displays an answer value.

@export var answer_value: int = 0
@export var is_correct: bool = false
@export var move_speed: float = 380.0

@onready var label: Label = $Label
@onready var platform_sprite: Sprite2D = $Sprite2D

func _ready() -> void:
	body_entered.connect(_on_body_entered)
	set_value(answer_value, is_correct)

func set_value(val: int, correct: bool) -> void:
	answer_value = val
	is_correct = correct
	if label:
		label.text = str(val)

func _process(delta: float) -> void:
	if GameManager.current_state != GameManager.GameState.RUNNING:
		return
	position.y += move_speed * delta

	if position.y > 2100.0:
		queue_free()

func _on_body_entered(body: Node2D) -> void:
	if not (body is MathRunnerPlayer):
		return
	if is_correct:
		ScoreManager.add_correct_answer_score()
		(body as MathRunnerPlayer).play_animation(MathRunnerPlayer.AnimState.CORRECT)
		if LevelManager.record_question_solved():
			GameManager.set_state(GameManager.GameState.LEVEL_COMPLETE)
	else:
		ScoreManager.apply_wrong_answer_penalty()
		(body as MathRunnerPlayer).play_animation(MathRunnerPlayer.AnimState.WRONG)
		if ScoreManager.current_lives <= 0:
			GameManager.set_state(GameManager.GameState.GAME_OVER)
