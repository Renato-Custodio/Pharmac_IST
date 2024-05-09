import { Body, Controller, Get, Param, Post, Query } from '@nestjs/common';
import { MedicineService } from './medicine.service';
import { Medicine } from '@prisma/client';
import { CreateMedicineDto } from './dtos/create-Medicine.dto';
import { LocationDto } from './dtos/location.dto';

@Controller('api/medicine')
export class MedicineController {
  constructor(private readonly medicineService: MedicineService) {}

  @Get('search')
  async findQuery(
    @Query('query') query: string,
    @Query('pageNumber') pageNumber?: number,
  ) {
    return await this.medicineService.getMedicines(query, pageNumber);
  }

  @Get(':medicineId/closestPharmacies')
  async getClosestPharmacies(
    @Body() location: LocationDto,
    @Param('medicineId') medicineId,
  ) {
    return await this.medicineService.getClosestPharmacies(
      medicineId,
      location,
    );
  }

  @Post()
  async createMedicine(
    @Body() createMedicineDto: CreateMedicineDto,
  ): Promise<Medicine> {
    return this.medicineService.createMedicine(createMedicineDto);
  }
}
