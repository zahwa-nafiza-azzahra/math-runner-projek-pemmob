extends Control

# level_complete_menu.gd

@onready var next_level_btn: Button = %NextLevelButton
@onready var replay_btn: Button = %ReplayButton
@onready var current_score_label: Label = %CurrentScoreLabel
@onready var best_score_label: Label = %BestScoreLabel
@onready var accuracy_label: Label = %AccuracyLabel

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	next_level_btn.pressed.connect(func(): GameManager.advance_to_next_level())
	replay_btn.pressed.connect(func(): GameManager.restart_current_level())

	GameManager.state_changed.connect(func(new_s, _old_s):
		if new_s == GameManager.GameState.LEVEL_COMPLETE:
			current_score_label.text = str(ScoreManager.score)
			best_score_label.text = str(ScoreManager.best_score)
			accuracy_label.text = str(ScoreManager.get_accuracy_percent()) + "%"
	)
