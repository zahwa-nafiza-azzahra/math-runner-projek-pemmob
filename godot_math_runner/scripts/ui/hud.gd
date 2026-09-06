extends Control

# hud.gd
# Manages Top Status Bar (Level, Score, Progress, Pause)
# and Bottom Question Panel with 4 Answer Pill Buttons.

@onready var level_label: Label = %LevelLabel
@onready var score_label: Label = %ScoreLabel
@onready var coins_label: Label = %CoinsLabel
@onready var lives_label: Label = %LivesLabel
@onready var progress_bar: ProgressBar = %ProgressBar
@onready var runner_icon: Label = %RunnerIcon
@onready var pause_button: Button = %PauseButton

@onready var question_container: Control = %QuestionContainer
@onready var equation_label: Label = %EquationLabel
@onready var answer_btn_0: Button = %AnswerBtn0
@onready var answer_btn_1: Button = %AnswerBtn1
@onready var answer_btn_2: Button = %AnswerBtn2
@onready var answer_btn_3: Button = %AnswerBtn3
@onready var stopped_banner: Control = %StoppedBanner

var answer_buttons: Array[Button] = []

func _ready() -> void:
	answer_buttons = [answer_btn_0, answer_btn_1, answer_btn_2, answer_btn_3]

	for i in range(answer_buttons.size()):
		var idx = i
		answer_buttons[i].pressed.connect(func(): _on_answer_button_pressed(idx))

	pause_button.pressed.connect(_on_pause_pressed)

	# Connect Autoload Signals
	ScoreManager.score_changed.connect(_on_score_changed)
	ScoreManager.coins_changed.connect(_on_coins_changed)
	ScoreManager.lives_changed.connect(_on_lives_changed)
	LevelManager.level_changed.connect(_on_level_changed)
	LevelManager.level_progress_updated.connect(_on_progress_updated)
	QuestionManager.question_generated.connect(_on_question_generated)
	GameManager.state_changed.connect(_on_game_state_changed)

	_refresh_all_hud()

func _unhandled_input(event: InputEvent) -> void:
	if GameManager.current_state != GameManager.GameState.QUESTION_ENCOUNTER:
		return

	# Desktop keyboard testing (Keys 1, 2, 3, 4)
	if event is InputEventKey and event.is_pressed():
		match event.keycode:
			KEY_1: _on_answer_button_pressed(0)
			KEY_2: _on_answer_button_pressed(1)
			KEY_3: _on_answer_button_pressed(2)
			KEY_4: _on_answer_button_pressed(3)

func _on_pause_pressed() -> void:
	GameManager.toggle_pause()

func _on_answer_button_pressed(index: int) -> void:
	if GameManager.current_state != GameManager.GameState.QUESTION_ENCOUNTER:
		return

	var is_correct = QuestionManager.evaluate_answer(index)

	if is_correct:
		# Correct Answer! Highlight Green
		_style_button(answer_buttons[index], Color(0.15, 0.8, 0.25))
		ScoreManager.add_correct_answer_score()
		var level_done = LevelManager.record_question_solved()

		await get_tree().create_timer(0.65).timeout
		_reset_button_styles()

		if level_done:
			GameManager.set_state(GameManager.GameState.LEVEL_COMPLETE)
		else:
			# Resume running!
			GameManager.set_state(GameManager.GameState.RUNNING)
	else:
		# Wrong Answer! Highlight Red
		_style_button(answer_buttons[index], Color(0.9, 0.2, 0.2))
		ScoreManager.apply_wrong_answer_penalty()

		await get_tree().create_timer(0.7).timeout
		_reset_button_styles()

		if ScoreManager.current_lives <= 0:
			GameManager.set_state(GameManager.GameState.GAME_OVER)

func _on_question_generated(q_data: Dictionary) -> void:
	equation_label.text = q_data.get("equation", "7 × 6 = ?")
	var options: Array = q_data.get("options", [36, 42, 48, 56])
	for i in range(answer_buttons.size()):
		if i < options.size():
			answer_buttons[i].text = str(options[i])
	_reset_button_styles()

func _on_game_state_changed(new_state: GameManager.GameState, old_state: GameManager.GameState) -> void:
	if new_state == GameManager.GameState.QUESTION_ENCOUNTER:
		question_container.visible = true
		stopped_banner.visible = true
	elif new_state == GameManager.GameState.RUNNING:
		question_container.visible = false
		stopped_banner.visible = false

func _on_score_changed(new_score: int) -> void:
	score_label.text = str(new_score)

func _on_coins_changed(new_coins: int) -> void:
	coins_label.text = str(new_coins)

func _on_lives_changed(new_lives: int) -> void:
	var hearts = ""
	for i in range(ScoreManager.max_lives):
		hearts += "❤️" if i < new_lives else "🖤"
	lives_label.text = hearts

func _on_level_changed(new_level: int) -> void:
	level_label.text = "LEVEL " + str(new_level)

func _on_progress_updated(cur: int, total: int) -> void:
	var ratio = LevelManager.get_progress_ratio()
	progress_bar.value = ratio * 100.0
	if runner_icon:
		runner_icon.position.x = 80.0 + ratio * 800.0

func _style_button(btn: Button, col: Color) -> void:
	btn.modulate = col

func _reset_button_styles() -> void:
	for btn in answer_buttons:
		btn.modulate = Color.WHITE

func _refresh_all_hud() -> void:
	_on_score_changed(ScoreManager.score)
	_on_coins_changed(ScoreManager.coins)
	_on_lives_changed(ScoreManager.current_lives)
	_on_level_changed(LevelManager.current_level)
	_on_progress_updated(LevelManager.questions_completed_in_level, LevelManager.total_questions_per_level)
	question_container.visible = (GameManager.current_state == GameManager.GameState.QUESTION_ENCOUNTER)
	stopped_banner.visible = question_container.visible
