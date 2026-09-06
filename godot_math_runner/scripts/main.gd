extends Node2D

# main.gd
# Orchestrates main gameplay scene nodes, modals, and game states.

@onready var background: Node2D = $Background
@onready var player: MathRunnerPlayer = $Player
@onready var hud: Control = $CanvasLayer/HUD
@onready var pause_menu: Control = $CanvasLayer/PauseMenu
@onready var game_over_menu: Control = $CanvasLayer/GameOverMenu
@onready var level_complete_menu: Control = $CanvasLayer/LevelCompleteMenu

func _ready() -> void:
	GameManager.state_changed.connect(_on_game_state_changed)
	_update_menu_visibility(GameManager.current_state)

func _on_game_state_changed(new_state: GameManager.GameState, _old_state: GameManager.GameState) -> void:
	_update_menu_visibility(new_state)

func _update_menu_visibility(state: GameManager.GameState) -> void:
	pause_menu.visible = (state == GameManager.GameState.PAUSED)
	game_over_menu.visible = (state == GameManager.GameState.GAME_OVER)
	level_complete_menu.visible = (state == GameManager.GameState.LEVEL_COMPLETE)
