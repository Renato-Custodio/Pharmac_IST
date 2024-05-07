import { GPSLocation } from '@prisma/client';
import { IsNotEmpty, IsObject, IsString } from 'class-validator';

export class CreatePharmacyDto {
  @IsString()
  @IsNotEmpty()
  name: string;

  @IsObject()
  @IsNotEmpty()
  location: GPSLocation;
}
