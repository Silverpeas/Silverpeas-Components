# Copyright (C) 2000 - 2026 Silverpeas
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# As a special exception to the terms and conditions of version 3.0 of
# the GPL, you may redistribute this Program in connection with Free/Libre
# Open Source Software ("FLOSS") applications as described in Silverpeas's
# FLOSS exception. You should have received a copy of the text describing
# the FLOSS exception, and it is also available here:
# "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.

"""Run after Maven package: python3 -m unittest discover -s projectManager/projectManager-war/src/test/python."""
import os
from pathlib import Path
import unittest
from zipfile import ZipFile


class ProjectManagerPackagingTest(unittest.TestCase):
  def test_component_does_not_replace_the_core_comment_engine(self):
    configured = os.environ.get("PROJECTMANAGER_WAR")
    candidates = [Path(configured)] if configured else list(
      (Path(__file__).resolve().parents[3] / "target").glob("*.war"))
    self.assertEqual(1, len(candidates), "Build the ProjectManager WAR first")
    with ZipFile(candidates[0]) as archive:
      libraries = [Path(name).name for name in archive.namelist()
        if name.startswith("WEB-INF/lib/") and name.endswith(".jar")]
    self.assertTrue(any(name.startswith("silverpeas-projectmanager-") for name in libraries))
    self.assertFalse(any(name.startswith("silverpeas-core-comment-") for name in libraries),
      "Core supplies its comment engine; bundling it here can overwrite the installed Core version")


if __name__ == "__main__":
  unittest.main()
