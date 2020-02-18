import unittest
from base import FunctionalTest
from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException

import time, os

from utils import standardize_colour


class NewVisitorTest(FunctionalTest, unittest.TestCase):

    def setUp(self):
        super().setUp()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls) -> None:
        super().tearDownClass()

    def test_user_can_see_correct_homepage(self):
        # New user John goes to predict website.
        self.browser.get(self.live_server_url)

        # John waits 2 seconds for website to load
        time.sleep(2)

        # John sees that "Predict Breast" is in browser title
        self.assertIn('Predict Breast', self.browser.title)

        # John sees Alison blue H1 title
        intro_title = self.browser.find_element_by_tag_name('h1')
        self.assertIn('What is Predict?', intro_title.text)
        self.assertEqual('rgba(0, 46, 93, 1)', standardize_colour(intro_title.value_of_css_property('color')))

        # John sees Start Predict button
        start_predict_button = self.browser.find_element_by_css_selector('button.btn-lg')
        self.assertIn('Start Predict', start_predict_button.text)

        # John sees version number on bottom right of the screen
        # (For dev only currently)
        build_number = self.browser.find_element_by_class_name('build-version')
        # self.assertEqual('Build: v0.0-dev-#000-hash', build_number.text)
        self.assertNotEqual('v0.0-dev-#000-hash', build_number.text)


class NewVisitorGDPRTest(FunctionalTest, unittest.TestCase):

    def setUp(self):
        super().setUp()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls) -> None:
        super().tearDownClass()

    def test_user_can_see_gdpr_sticky_div(self):
        # New user John goes to predict website.
        self.browser.get(self.live_server_url)

        # John waits 1 seconds for website to load
        time.sleep(1)

        # John sees sticky GDPR bar on bottom of the page. See it's dark color.
        gdpr_bar = self.browser.find_element_by_class_name('gdpr-container')
        self.assertEqual('block', gdpr_bar.value_of_css_property('display'))
        self.assertEqual('rgba(29, 29, 29, 1)', standardize_colour(gdpr_bar.value_of_css_property('color')))

        # John sees sticky GDPR bar has Allow button
        gdpr_allow_input = self.browser.find_element_by_id('submit-btn-allow')
        self.assertEqual('Allow', gdpr_allow_input.get_attribute('value'))

        # John sees sticky GDPR bar has Don't Allow button
        gdpr_not_allow_input = self.browser.find_element_by_id('submit-btn-not-allow')
        self.assertEqual("Don't Allow", gdpr_not_allow_input.get_attribute('value'))

    def test_user_can_not_allow_gdpr_terms_conditions_div(self):
        # New user John goes to predict website.
        self.browser.get(self.live_server_url)

        # John waits 1 seconds for website to load
        time.sleep(1)

        # John dont not allows cookie
        gdpr_not_allow_input = self.browser.find_element_by_id('submit-btn-not-allow')
        self.assertEqual("Don't Allow", gdpr_not_allow_input.get_attribute('value'))

        # Before clicking ok, gdpr div is still visible
        gdpr_bar = self.browser.find_element_by_class_name('gdpr-container')
        self.assertEqual('block', gdpr_bar.value_of_css_property('display'))
        gdpr_not_allow_input.click()

        # After clicking ok, gdpr div is no longer visible
        time.sleep(1)
        self.assertEqual('none', gdpr_bar.value_of_css_property('display'))

    def test_user_can_agree_gdpr_analytics_and_terms_conditions_div(self):
        # New user John goes to predict website.
        self.browser.get(self.live_server_url)

        # John waits 1 seconds for website to load
        time.sleep(1)

        # John clicks Allow cookies
        gdpr_allow_input = self.browser.find_element_by_id('submit-btn-allow')
        self.assertEqual('Allow', gdpr_allow_input.get_attribute('value'))

        # Before clicking ok, gdpr div is still visible
        gdpr_bar = self.browser.find_element_by_class_name('gdpr-container')
        self.assertEqual('block', gdpr_bar.value_of_css_property('display'))
        gdpr_allow_input.click()
        # After clicking I agree, gdpr div is no longer visible
        time.sleep(1)
        self.assertEqual('none', gdpr_bar.value_of_css_property('display'))

    def test_user_agreed_to_tracking_sees_hotjar(self):
        # Initial condition to have GPDR enabled
        self.browser.get(self.live_server_url)
        # John waits 1 seconds for website to load
        time.sleep(1)
        gdpr_allow_input = self.browser.find_element_by_id('submit-btn-allow')
        self.assertEqual('Allow', gdpr_allow_input.get_attribute('value'))
        gdpr_allow_input.click()

        # internet explorer Hotjar disabled for now.
        if os.getenv('BROWSER') == 'internet explorer':
            print('Ignoring test for hotjar it appears support for IE9 is dropped')
        if not os.getenv('BROWSER') == 'internet explorer':
            try:
                # John can see hotjar popping up. (If this doesn't exist, it means hotjar hasn't come through and there is error)
                print("hotjar_injected_div waiting for ... ")
                hotjar_injected_div = WebDriverWait(self.browser, 20).until(
                    EC.presence_of_element_located((By.ID, "_hj_poll_container"))
                )
                print("found hotjar_injected_div")
            except TimeoutException as e:
                print(f"Waited 20 second but can not find hotjar_injected_div - {e} - {os.getenv('BROWSER')}")
                raise TimeoutException

        time.sleep(5)


class NewVisitorGDPROldSiteTest(FunctionalTest, unittest.TestCase):

    def setUp(self):
        super().setUp()

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls) -> None:
        super().tearDownClass()

    def test_user_can_see_gdpr_sticky_div(self):
        # New user John goes to predict website.
        self.browser.get(self.live_server_url + 'all_versions.html')

        # John waits 1 seconds for website to load
        time.sleep(1)

        # John sees sticky GDPR bar on bottom of the page. See it's dark color.
        gdpr_bar = self.browser.find_element_by_class_name('gdpr-container')
        self.assertEqual('block', gdpr_bar.value_of_css_property('display'))
        self.assertEqual('rgba(29, 29, 29, 1)', standardize_colour(gdpr_bar.value_of_css_property('color')))

        # John sees sticky GDPR bar has Allow button
        gdpr_allow_input = self.browser.find_element_by_id('submit-btn-allow')
        self.assertEqual('Allow', gdpr_allow_input.get_attribute('value'))

        # John sees sticky GDPR bar has Don't Allow button
        gdpr_not_allow_input = self.browser.find_element_by_id('submit-btn-not-allow')
        self.assertEqual("Don't Allow", gdpr_not_allow_input.get_attribute('value'))


    def test_user_can_not_allow_gdpr_terms_conditions_div(self):
        # New user John goes to predict website.
        self.browser.get(self.live_server_url + 'all_versions.html')

        # John waits 1 seconds for website to load
        time.sleep(1)

        # John dont not allows cookie
        gdpr_not_allow_input = self.browser.find_element_by_id('submit-btn-not-allow')
        self.assertEqual("Don't Allow", gdpr_not_allow_input.get_attribute('value'))

        # Before clicking ok, gdpr div is still visible
        gdpr_bar = self.browser.find_element_by_class_name('gdpr-container')
        self.assertEqual('block', gdpr_bar.value_of_css_property('display'))
        gdpr_not_allow_input.click()

        # After clicking ok, gdpr div is no longer visible
        time.sleep(1)
        self.assertEqual('none', gdpr_bar.value_of_css_property('display'))

    def test_user_can_agree_gdpr_analytics_and_terms_conditions_div(self):
        # New user John goes to predict website.
        self.browser.get(self.live_server_url + 'all_versions.html')

        # John waits 1 seconds for website to load
        time.sleep(1)

        # John clicks Allow cookies
        gdpr_allow_input = self.browser.find_element_by_id('submit-btn-allow')
        self.assertEqual('Allow', gdpr_allow_input.get_attribute('value'))

        # Before clicking ok, gdpr div is still visible
        gdpr_bar = self.browser.find_element_by_class_name('gdpr-container')
        self.assertEqual('block', gdpr_bar.value_of_css_property('display'))
        gdpr_allow_input.click()
        # After clicking I agree, gdpr div is no longer visible
        time.sleep(1)
        self.assertEqual('none', gdpr_bar.value_of_css_property('display'))

    def test_user_agreed_to_tracking_sees_hotjar(self):
        # Initial condition to have GPDR enabled
        self.browser.get(self.live_server_url + 'all_versions.html')
        gdpr_allow_input = self.browser.find_element_by_id('submit-btn-allow')
        self.assertEqual('Allow', gdpr_allow_input.get_attribute('value'))
        gdpr_allow_input.click()

        # Go to homepage where Hotjar pops up
        self.browser.get(self.live_server_url)

        # internet explorer Hotjar disabled for now.
        if os.getenv('BROWSER') == 'internet explorer':
            print('Ignoring test for hotjar it appears support for IE9 is dropped')
        if not os.getenv('BROWSER') == 'internet explorer':
            try:
                # John can see hotjar popping up. (If this doesn't exist, it means hotjar hasn't come through and there is error)
                print("hotjar_injected_div waiting for ... ")
                hotjar_injected_div = WebDriverWait(self.browser, 20).until(
                    EC.presence_of_element_located((By.ID, "_hj_poll_container"))
                )
                print("found hotjar_injected_div")
            except TimeoutException as e:
                print(f"Waited 20 second but can not find hotjar_injected_div - {e} - {os.getenv('BROWSER')}")
                raise TimeoutException

        time.sleep(5)


class NewVisitorCanUseTools(FunctionalTest, unittest.TestCase):

    def setUp(self):
        super().setUp()
        # New user John goes to predict website.
        self.browser.get(self.live_server_url)

        # John waits 2 seconds for website to load
        time.sleep(2)

        # Temp work around for locally hosted dev test
        # John sees Start Predict button and clicks it.
        start_predict_button = self.browser.find_element_by_css_selector('button.btn-lg')
        start_predict_button.click()

        # John waits 2 seconds for tools page to load
        time.sleep(2)

    def tearDown(self):
        super().tearDown()

    @classmethod
    def tearDownClass(cls) -> None:
        super().tearDownClass()

    def test_user_does_not_see_results_initially(self):
        #results
        info_disabled_text_icon = self.browser.find_element_by_class_name('fa-info-circle')
        info_disabled_text = info_disabled_text_icon.find_element_by_xpath('..')
        self.assertIn('Treatment options and results will appear here when you have filled in all the information needed above.'
                      , info_disabled_text.text)

    def test_user_add_sensible_input_and_see_results(self):
        # John add details into the page

        input_post_menopausal = self.browser.find_element_by_xpath('//div[@aria-label="Tumour is DCIS or LCIS radio button group"]/button[2]')
        input_post_menopausal.click()

        input_age = self.browser.find_element_by_id('age')
        input_age.click()
        input_age.clear()
        # Work around for chrome
        input_age.send_keys("5")
        input_age.send_keys("2")

        input_post_menopausal = self.browser.find_element_by_xpath('//div[@aria-label="Post Menopausal? radio button group"]/button[2]')
        input_post_menopausal.click()

        input_er_status = self.browser.find_element_by_xpath('//div[@aria-label="ER status radio button group"]/button[1]')
        input_er_status.click()

        input_her2_status = self.browser.find_element_by_xpath('//div[@aria-label="HER2 status radio button group"]/button[1]')
        input_her2_status.click()

        input_ki67_status = self.browser.find_element_by_xpath('//div[@aria-label="Ki-67 status radio button group"]/button[2]')
        input_ki67_status.click()

        input_tumour_size = self.browser.find_element_by_id('size')
        input_tumour_size.clear()
        input_tumour_size.send_keys("3")

        input_tumour_grade = self.browser.find_element_by_xpath('//div[@aria-label="Tumour grade radio button group"]/button[3]')
        input_tumour_grade.click()

        input_detected_by = self.browser.find_element_by_xpath('//div[@aria-label="Detected by radio button group"]/button[1]')
        input_detected_by.click()

        input_age = self.browser.find_element_by_id('nodes')
        input_age.clear()
        input_age.send_keys("2")

        time.sleep(1)

        # John scroll down to see results. John can see new heads have appeared.
        self.browser.execute_script("window.scrollTo(0, document.body.scrollHeight);")

        h3_tags = self.browser.find_elements_by_tag_name('h3')

        h3_tag_options = h3_tags[0]
        h3_tag_result = h3_tags[1]

        self.assertIn('Treatment Options', h3_tag_options.text)
        self.assertIn('Results', h3_tag_result.text)

        time.sleep(5)
        # # How to distinguish between results show or not showing?


if __name__ == '__main__':
    unittest.main(warnings='ignore')
