import os
import unittest
import time
from datetime import datetime

from selenium import webdriver
from selenium.common.exceptions import WebDriverException


MAX_WAIT = 10


def wait(fn):
    def modified_fn(*args, **kwargs):
        start_time = time.time()
        while True:
            try:
                return fn(*args, **kwargs)
            except (AssertionError, WebDriverException) as e:
                if time.time() - start_time > MAX_WAIT:
                    raise e
                time.sleep(0.5)
    return modified_fn


SCREEN_DUMP_LOCATION = os.path.join(
    os.path.dirname(os.path.abspath(__file__)), 'screendumps'
)


class FunctionalTest(unittest.TestCase):

    def setUp(self):
        print('setup from FunctionalTest, baseclass')
        self.browser = webdriver.Firefox()
        # self.staging_server = os.environ.get('STAGING_SERVER')
        # if self.staging_server:
        #     self.live_server_url = 'http://' + self.staging_server

    def tearDown(self):
        if self._test_has_failed():
            if not os.path.exists(SCREEN_DUMP_LOCATION):
                os.makedirs(SCREEN_DUMP_LOCATION)
            for ix, handle in enumerate(self.browser.window_handles):
                self._windowid = ix
                self.browser.switch_to_window(handle)
                self.take_screenshot()
                self.dump_html()
        self.browser.quit()
        super().tearDown()

    def _test_has_failed(self):
        # slightly obscure but couldn't find a better way!
        return any(error for (method, error) in self._outcome.errors)

    def take_screenshot(self):
        filename = self._get_filename() + '.png'
        print('screenshotting to', filename)
        self.browser.get_screenshot_as_file(filename)

    def dump_html(self):
        filename = self._get_filename() + '.html'
        print('dumping page HTML to', filename)
        with open(filename, 'w') as f:
            f.write(self.browser.page_source)

    def _get_filename(self):
        timestamp = datetime.now().isoformat().replace(':', '.')[:19]
        return '{folder}/{classname}.{method}-window{windowid}-{timestamp}'.format(
            folder=SCREEN_DUMP_LOCATION,
            classname=self.__class__.__name__,
            method=self._testMethodName,
            windowid=self._windowid,
            timestamp=timestamp
        )

    @wait
    def wait_for(self, fn):
        return fn()


# import unittest
# from .base import FunctionalTest
# from selenium import webdriver
# from selenium.webdriver.common.keys import Keys


class NewVisitorTest(FunctionalTest, unittest.TestCase):

    def setUp(self):
        print('setup from NewVisitorTest')
        super().setUp()

    def tearDown(self):
        super().tearDown()

    def user_can_see_gdpr_sticky_div(self):
        # New user John goes to predict website.
        self.browser.get(self.live_server_url)
        self.fail('can fail')
        print('hello')


if __name__ == '__main__':
    unittest.main()
    # unittest.main(warnings='ignore')
