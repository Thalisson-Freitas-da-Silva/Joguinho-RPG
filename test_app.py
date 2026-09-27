import unittest

from app import Farm


class FarmTest(unittest.TestCase):
    def test_planting_and_harvesting_replenishes_resources(self):
        farm = Farm()
        farm.player = [120, 80]
        farm.interact(); farm.interact()
        self.assertEqual(farm.plots[0], 2)
        farm.next_day(); farm.interact()
        self.assertEqual(farm.plots[0], 0)
        self.assertEqual((farm.crops, farm.wood, farm.seeds), (1, 9, 7))

    def test_building_the_barn_unlocks_animals(self):
        farm = Farm(); farm.player = [262, 58]
        farm.interact()
        self.assertEqual((farm.barn, farm.wood), (1, 3))
        farm.wood, farm.crops = 9, 2
        farm.interact()
        self.assertEqual(farm.barn, 2)

    def test_reset_restores_initial_farm(self):
        farm = Farm(); farm.wood = 0; farm.house = 2; farm.plots[0] = 3
        farm.reset()
        self.assertEqual((farm.day, farm.wood, farm.house, farm.plots[0]), (1, 8, 0, 0))

    def test_talking_to_lia_opens_and_advances_dialogue(self):
        farm = Farm()
        farm.player = [191, 79]
        farm.interact()
        self.assertEqual(farm.dialogue["speaker"], "Lia")
        farm.interact(); farm.interact()
        self.assertIsNone(farm.dialogue)


if __name__ == "__main__":
    unittest.main()
