extends Control

# pause_menu.gd

@onready var resume_btn: Button = %ResumeButton
@onready var restart_btn: Button = %RestartButton
@onready var exit_btn: Button = %ExitButton

func _ready() -> void:
	process_mode = Node.PROCESS_MODE_ALWAYS
	resume_btn.pressed.connect(func(): GameManager.set_state(GameManager.GameState.RUNNING))
	restart_btn.pressed.connect(func(): GameManager.restart_current_level())
	exit_btn.pressed.connect(func(): GameManager.start_new_game())
