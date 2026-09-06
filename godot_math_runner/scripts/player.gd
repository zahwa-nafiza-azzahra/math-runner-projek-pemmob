extends CharacterBody2D
class_name MathRunnerPlayer

# player.gd
# 2D Sprite Character Controller implementing the 7 core animations:
# 1. Idle, 2. Run, 3. Jump, 4. Collect Coin, 5. Correct Answer, 6. Wrong Answer, 7. Victory.

enum AnimState {
	IDLE,
	RUN,
	JUMP,
	COLLECT,
	CORRECT,
	WRONG,
	VICTORY
}

@export var lane_width: float = 220.0
@export var base_y: float = 1350.0

@onready var sprite: AnimatedSprite2D = $AnimatedSprite2D
@onready var shadow: Sprite2D = $Shadow
@onready var fx_label: Label = $FxLabel
@onready var anim_player: AnimationPlayer = $AnimationPlayer

var current_lane: int = 0 # -1 = Left, 0 = Center, 1 = Right
var current_anim_state: AnimState = AnimState.RUN
var lane_tween: Tween
var is_jumping: bool = false

func _ready() -> void:
	position = Vector2(540.0, base_y) # Centered for 1080x1920 portrait
	play_animation(AnimState.RUN)

	# Connect Autoload signals
	QuestionManager.answer_evaluated.connect(_on_answer_evaluated)
	GameManager.state_changed.connect(_on_game_state_changed)

func _unhandled_input(event: InputEvent) -> void:
	if GameManager.current_state != GameManager.GameState.RUNNING:
		return

	# Lane steering via input actions or swipe drag
	if event.is_action_pressed("move_left"):
		move_lane(-1)
	elif event.is_action_pressed("move_right"):
		move_lane(1)
	elif event.is_action_pressed("jump"):
		perform_jump()

	# Handle mobile touch drag
	if event is InputEventScreenDrag:
		if event.relative.x < -20:
			move_lane(-1)
		elif event.relative.x > 20:
			move_lane(1)
		elif event.relative.y < -25:
			perform_jump()

func move_lane(dir: int) -> void:
	var target_lane = clampi(current_lane + dir, -1, 1)
	if target_lane == current_lane:
		return
	current_lane = target_lane
	var target_x = 540.0 + float(current_lane) * lane_width

	if lane_tween and lane_tween.is_valid():
		lane_tween.kill()
	lane_tween = create_tween().set_trans(Tween.TRANS_SPRING).set_ease(Tween.EASE_OUT)
	lane_tween.tween_property(self, "position:x", target_x, 0.22)

func play_animation(state: AnimState) -> void:
	current_anim_state = state
	match state:
		AnimState.IDLE:
			sprite.play("idle")
		AnimState.RUN:
			sprite.play("run")
		AnimState.JUMP:
			sprite.play("jump")
		AnimState.COLLECT:
			sprite.play("collect")
			_show_floating_fx("✨ +COIN!", Color(1.0, 0.85, 0.2))
		AnimState.CORRECT:
			sprite.play("correct")
			_show_floating_fx("⭐ PERFECT!", Color(0.2, 0.9, 0.3))
		AnimState.WRONG:
			sprite.play("wrong")
			_show_floating_fx("💥 OUCH!", Color(1.0, 0.3, 0.3))
		AnimState.VICTORY:
			sprite.play("victory")
			_show_floating_fx("🏆 VICTORY!", Color(1.0, 0.9, 0.1))

func perform_jump() -> void:
	if is_jumping or current_anim_state != AnimState.RUN:
		return
	is_jumping = true
	play_animation(AnimState.JUMP)

	var jump_tween = create_tween().set_trans(Tween.TRANS_QUAD).set_ease(Tween.EASE_OUT)
	jump_tween.tween_property(sprite, "position:y", -90.0, 0.28)
	jump_tween.chain().set_ease(Tween.EASE_IN).tween_property(sprite, "position:y", 0.0, 0.24)
	jump_tween.finished.connect(func():
		is_jumping = false
		if GameManager.current_state == GameManager.GameState.RUNNING:
			play_animation(AnimState.RUN)
	)

func _on_answer_evaluated(is_correct: bool, chosen_index: int, correct_index: int) -> void:
	if is_correct:
		play_animation(AnimState.CORRECT)
		perform_jump()
	else:
		play_animation(AnimState.WRONG)
		# Shake player
		var shake = create_tween()
		shake.tween_property(sprite, "position:x", 15.0, 0.05)
		shake.tween_property(sprite, "position:x", -15.0, 0.05)
		shake.tween_property(sprite, "position:x", 0.0, 0.05)

func _on_game_state_changed(new_state: GameManager.GameState, old_state: GameManager.GameState) -> void:
	match new_state:
		GameManager.GameState.RUNNING:
			play_animation(AnimState.RUN)
		GameManager.GameState.QUESTION_ENCOUNTER:
			play_animation(AnimState.IDLE)
		GameManager.GameState.LEVEL_COMPLETE:
			play_animation(AnimState.VICTORY)
		GameManager.GameState.GAME_OVER:
			play_animation(AnimState.WRONG)

func _show_floating_fx(text: String, color: Color) -> void:
	if not fx_label:
		return
	fx_label.text = text
	fx_label.modulate = color
	fx_label.position = Vector2(-70, -140)
	fx_label.scale = Vector2(0.5, 0.5)
	fx_label.visible = true

	var t = create_tween().set_parallel(true)
	t.tween_property(fx_label, "position:y", -210.0, 0.6)
	t.tween_property(fx_label, "scale", Vector2(1.2, 1.2), 0.2).set_ease(Tween.EASE_OUT)
	t.tween_property(fx_label, "modulate:a", 0.0, 0.6).set_delay(0.2)
	t.chain().tween_callback(func(): fx_label.visible = false)
