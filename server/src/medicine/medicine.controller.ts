import { Body, Controller, Get, Param, Post, Query } from '@nestjs/common';
import { MedicineService } from './medicine.service';
import { Medicine } from '@prisma/client';
import { CreateMedicineDto } from './dtos/create-medicine.dto';

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
    @Param('medicineId') medicineId: string,
    @Query('lat') latitude: string,
    @Query('lng') longitude: string,
  ) {
    return await this.medicineService.getClosestPharmacies(
      medicineId,
      parseInt(latitude),
      parseInt(longitude),
    );
  }

  @Post()
  async createMedicine(
    @Body() createMedicineDto: CreateMedicineDto,
  ): Promise<Medicine> {
    return this.medicineService.createMedicine(createMedicineDto);
  }
}
