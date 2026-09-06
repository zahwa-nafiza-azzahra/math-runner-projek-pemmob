extends Node

# QuestionManager.gd
# Generates arithmetic questions with 1 guaranteed correct answer and 3 random distractors.

signal question_generated(question_data: Dictionary)
signal answer_evaluated(is_correct: bool, chosen_index: int, correct_index: int)

var current_question: Dictionary = {}

func generate_question() -> Dictionary:
	var config = LevelManager.get_level_config()
	var operations: Array = config.get("operations", ["+"])
	var op: String = operations[randi() % operations.size()]
	var min_n: int = config.get("min_num", 1)
	var max_n: int = config.get("max_num", 10)

	var num1: int = 1
	var num2: int = 1
	var correct_answer: int = 2

	match op:
		"+":
			num1 = randi_range(min_n, max_n)
			num2 = randi_range(min_n, max_n)
			correct_answer = num1 + num2
		"-":
			num1 = randi_range(min_n + 2, max_n + 5)
			num2 = randi_range(min_n, num1 - 1)
			correct_answer = num1 - num2
		"×":
			num1 = randi_range(2, mini(max_n, 12))
			num2 = randi_range(2, mini(max_n, 10))
			correct_answer = num1 * num2
		"÷":
			num2 = randi_range(2, 9)
			var mult = randi_range(2, 10)
			num1 = num2 * mult
			correct_answer = mult

	var equation_text: String = "%d %s %d = ?" % [num1, op, num2]

	# Generate 3 distinct incorrect answers (distractors)
	var wrong_options: Array[int] = []
	var attempts: int = 0
	while wrong_options.size() < 3 and attempts < 100:
		attempts += 1
		var delta: int = randi_range(-8, 8)
		if delta == 0:
			delta = 1 if randf() > 0.5 else -1
		var candidate: int = correct_answer + delta
		if candidate > 0 and candidate != correct_answer and not wrong_options.has(candidate):
			wrong_options.append(candidate)

	# Fallback if numbers are small
	while wrong_options.size() < 3:
		var candidate = correct_answer + wrong_options.size() + 1
		if not wrong_options.has(candidate):
			wrong_options.append(candidate)

	# Shuffle all 4 options
	var all_options: Array = []
	all_options.append_array(wrong_options)
	var correct_pos: int = randi() % 4
	all_options.insert(correct_pos, correct_answer)

	current_question = {
		"equation": equation_text,
		"options": all_options,
		"correct_index": correct_pos,
		"correct_answer": correct_answer
	}

	question_generated.emit(current_question)
	return current_question

func evaluate_answer(chosen_index: int) -> bool:
	if current_question.is_empty():
		return false
	var is_correct = (chosen_index == current_question.get("correct_index", -1))
	answer_evaluated.emit(is_correct, chosen_index, current_question.get("correct_index", 0))
	return is_correct
