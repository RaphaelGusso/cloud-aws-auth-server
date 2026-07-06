import boto3
from io import BytesIO

import thumb

PUBLIC_BUCKET = "raphaelgusso-authserver-public"
s3 = boto3.client("s3")

IMAGE_SIZES = {
    "xl": (256, 256),
    "l": (128, 128),
    "m": (64, 64),
    "s": (32, 32),
}


def process(img):
    print(f"Downloading {img['key']}")
    src_image = BytesIO()
    s3.download_fileobj(Bucket=img["bucket"], Key=img["key"], Fileobj=src_image)
    for size, dimensions in IMAGE_SIZES.items():
        dest_image = thumb.resize(src_image, dimensions)
        dest_filename = thumb.create_filename(img["name"], size)
        s3.upload_fileobj(
            Bucket=PUBLIC_BUCKET,
            Key=dest_filename,
            Fileobj=dest_image,
            ExtraArgs={"ContentType": "image/png"},
        )
    s3.delete_object(Bucket=img["bucket"], Key=img["key"])


def lambda_handler(event, context):
    print(f"Received event: {event}")
    for img in thumb.find_records(event):
        process(img)

    return {"statusCode": 200}
