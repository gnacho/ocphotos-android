package app.ocphotos.data

import app.ocphotos.data.local.AlbumEntity
import app.ocphotos.data.local.AssetEntity
import app.ocphotos.data.remote.dto.AlbumDto
import app.ocphotos.data.remote.dto.AssetDto

fun AssetDto.toEntity(): AssetEntity = AssetEntity(
    id = id,
    path = path,
    filename = filename,
    mediaType = mediaType,
    takenAt = takenAt,
    width = width,
    height = height,
    camera = camera,
    lens = lens,
    iso = iso,
    aperture = aperture,
    shutter = shutter,
    focal = focal,
    lat = lat,
    lon = lon,
    size = size,
    favorite = favorite,
    archived = archived,
)

fun AlbumDto.toEntity(): AlbumEntity = AlbumEntity(
    id = id,
    name = name,
    count = count,
    coverId = coverId,
    createdAt = createdAt,
)
