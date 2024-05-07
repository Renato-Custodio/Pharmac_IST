import { IsNotEmpty, IsPositive, IsString } from 'class-validator';

export class CreateStockDto {
  @IsString()
  @IsNotEmpty()
  medicineID: string;

  @IsPositive()
  @IsNotEmpty()
  quantity: number;

  @IsString()
  @IsNotEmpty()
  pharmacyId: string;
}
