import { Pharmacy } from '@prisma/client';
import { IsNotEmpty, IsPositive, IsString } from 'class-validator';

export class CreateMedicineDto {
  @IsString()
  @IsNotEmpty()
  name: string;

  @IsString()
  @IsNotEmpty()
  purpose: string;

  @IsPositive()
  @IsNotEmpty()
  quantity: number;
}

export class ReturnPharmaciesDto {
  pharmacy: Partial<Pharmacy>;
  distance: number;
}
