from datetime import datetime
from unittest import TestCase
from uuid import uuid4

import thumb


def mock_event(key):
    return {
        "eventVersion": "2.1",
        "eventSource": "aws:s3",
        "awsRegion": "us-east-1",
        "eventTime": "2023-11-24T18:25:40.498Z",
        "eventName": "ObjectCreated:Put",
        "userIdentity": {"principalId": "AWS:AIDAQCQ6C5XVUVGST3RGO"},
        "requestParameters": {"sourceIPAddress": "24.239.160.171"},
        "responseElements": {
            "x-amz-request-id": "YS31G5P6S8DC80KR",
            "x-amz-id-2": "JiTeTaQg38jyoz2scNGXdX8PnFGXpQy4jcjk3BQ4aPsU3BV8YyYVwll6y1HK3gV7GRs3V/CIfxV/s=",
        },
        "s3": {
            "s3SchemaVersion": "1.0",
            "configurationId": "e7854a82-30da-4aca-9d8f-5f3dcb4f5adb",
            "bucket": {
                "name": "test-bucket",
                "ownerIdentity": {"principalId": "A3C6UQNWHN7FON"},
                "arn": "arn:aws:s3:::test-bucket",
            },
            "object": {
                "key": key,
                "size": 60928,
                "eTag": uuid4(),
                "sequencer": uuid4(),
            },
        },
    }


class ThumbTest(TestCase):
    def test_find_records_returns_null_for_invalid(self):
        self.assertEqual([], list(thumb.find_records({"Invalid": []})))

    def test_find_records_returns_images(self):
        event = {
            "Records": [
                mock_event(key="/a/file1.jpg"),
                {"sns": {}},
                mock_event(key="/b/file.txt"),
                mock_event(key="/c/d/FILE3.PNG"),
            ]
        }
        expected = [
            {
                "bucket": "test-bucket",
                "key": "/a/file1.jpg",
                "name": "/a/file1",
                "ext": ".jpg",
            },
            {
                "bucket": "test-bucket",
                "key": "/c/d/FILE3.PNG",
                "name": "/c/d/FILE3",
                "ext": ".png",
            },
        ]
        self.assertEqual(expected, list(thumb.find_records(event)))

    def test_create_filename(self):
        self.assertEqual(
            "avatars/a/xl_user.png", thumb.create_filename("avatars/a/user", "xl")
        )
