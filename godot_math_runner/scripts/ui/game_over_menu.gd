extends Control

# game_over_menu.gd

@onready var try_again_btn: Button = %TryAgainButton
@onready var menu_btn: Button = %MenuButton
@onready var score_label: Label = %FinalScoreLabel

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	try_again_btn.pressed.connect(func(): GameManager.restart_current_level())
	menu_btn.pressed.connect(func(): GameManager.start_new_game())
	GameManager.state_changed.connect(func(new_s, _old_s):
		if new_s == GameManager.GameState.GAME_OVER:
			score_label.text = "Final Score: " + str(ScoreManager.score)
	)
