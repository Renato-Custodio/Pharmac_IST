import { IsNotEmpty, IsNumber } from 'class-validator';

export class LocationDto {
  @IsNumber()
  @IsNotEmpty()
  longitude: number;

  @IsNumber()
  @IsNotEmpty()
  latitude: number;
}
